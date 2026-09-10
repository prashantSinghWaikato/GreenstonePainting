package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminNotificationPreferencesControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository userRepository;

    @BeforeEach
    void createStaff() {
        userRepository.save(new AdminUser(
                "alerts@greenstonepainting.co.nz", "test-password-hash", "Alerts Tester"
        ));
    }

    @Test
    void readsAndUpdatesTheCurrentStaffMembersPreferences() throws Exception {
        mockMvc.perform(get("/api/admin/account/notifications")
                        .with(user("alerts@greenstonepainting.co.nz").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentNotificationsEnabled").value(true))
                .andExpect(jsonPath("$.followUpNotificationsEnabled").value(true))
                .andExpect(jsonPath("$.dailyDigestEnabled").value(true))
                .andExpect(jsonPath("$.jobNotificationsEnabled").value(true));

        mockMvc.perform(patch("/api/admin/account/notifications")
                        .with(user("alerts@greenstonepainting.co.nz").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignmentNotificationsEnabled": false,
                                  "followUpNotificationsEnabled": true,
                                  "dailyDigestEnabled": false,
                                  "jobNotificationsEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentNotificationsEnabled").value(false))
                .andExpect(jsonPath("$.followUpNotificationsEnabled").value(true))
                .andExpect(jsonPath("$.dailyDigestEnabled").value(false))
                .andExpect(jsonPath("$.jobNotificationsEnabled").value(false));
    }

    @Test
    void protectsPreferenceReadsAndWrites() throws Exception {
        mockMvc.perform(get("/api/admin/account/notifications"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/admin/account/notifications")
                        .with(user("alerts@greenstonepainting.co.nz").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignmentNotificationsEnabled": true,
                                  "followUpNotificationsEnabled": true,
                                  "dailyDigestEnabled": true,
                                  "jobNotificationsEnabled": true
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
