package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.AdminRole;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.repository.AdminAccountActivityRepository;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminStaffControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository userRepository;

    @Autowired
    private AdminAccountActivityRepository activityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AdminUser owner;
    private AdminUser staff;

    @BeforeEach
    void createTeam() {
        owner = userRepository.saveAndFlush(new AdminUser(
                "owner@greenstonepainting.co.nz",
                passwordEncoder.encode("owner-password-123"),
                "Greenstone Owner",
                AdminRole.OWNER
        ));
        staff = userRepository.saveAndFlush(new AdminUser(
                "staff@greenstonepainting.co.nz",
                passwordEncoder.encode("staff-password-123"),
                "Office Staff",
                AdminRole.STAFF
        ));
    }

    @Test
    void letsOwnersCreateStaffAndRecordsTheSecurityActivity() throws Exception {
        mockMvc.perform(post("/api/admin/staff")
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Project Coordinator",
                                  "email": "coordinator@greenstonepainting.co.nz",
                                  "role": "STAFF",
                                  "temporaryPassword": "temporary-pass-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.enabled").value(true));

        var created = userRepository.findByEmailIgnoreCase("coordinator@greenstonepainting.co.nz").orElseThrow();
        assertThat(passwordEncoder.matches("temporary-pass-123", created.getPasswordHash())).isTrue();
        assertThat(activityRepository.findTop50ByOrderByCreatedAtDesc())
                .anyMatch(activity -> activity.getSummary().contains("Project Coordinator"));
    }

    @Test
    void preventsStaffFromManagingTeamAccounts() throws Exception {
        mockMvc.perform(get("/api/admin/staff")
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF")))
                .andExpect(status().isForbidden());
    }

    @Test
    void letsStaffChangeOnlyTheirOwnPassword() throws Exception {
        mockMvc.perform(patch("/api/admin/account/password")
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "staff-password-123",
                                  "newPassword": "new-staff-password-456"
                                }
                                """))
                .andExpect(status().isNoContent());

        var updated = userRepository.findByEmailIgnoreCase(staff.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches("new-staff-password-456", updated.getPasswordHash())).isTrue();
        assertThat(updated.getPasswordChangedAt()).isNotNull();
    }

    @Test
    void ownerCannotDeactivateTheirOwnAccount() throws Exception {
        mockMvc.perform(patch("/api/admin/staff/{staffId}/enabled", owner.getId())
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled": false, "version": %d}
                                """.formatted(owner.getVersion())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot deactivate your own account."));
    }

    @Test
    void deactivatedAccountsAreRejectedOnTheirNextAdminRequest() throws Exception {
        staff.setEnabled(false);
        userRepository.saveAndFlush(staff);

        mockMvc.perform(get("/api/admin/auth/me")
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("This staff account is no longer active."));
    }
}
