package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.AdminRole;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
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
class AdminServiceContentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository userRepository;

    @Autowired
    private ServiceOfferingRepository serviceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AdminUser owner;
    private AdminUser staff;

    @BeforeEach
    void createTeam() {
        owner = userRepository.saveAndFlush(new AdminUser(
                "service-owner@greenstonepainting.co.nz", passwordEncoder.encode("owner-password-123"),
                "Service Owner", AdminRole.OWNER
        ));
        staff = userRepository.saveAndFlush(new AdminUser(
                "service-staff@greenstonepainting.co.nz", passwordEncoder.encode("staff-password-123"),
                "Service Staff", AdminRole.STAFF
        ));
    }

    @Test
    void migrationPreservesPublishedServiceContent() throws Exception {
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("interior-painting"))
                .andExpect(jsonPath("$[0].inclusions.length()").value(4))
                .andExpect(jsonPath("$[0].imageUrl").value("/images/greenstone-bedroom.webp"));
    }

    @Test
    void staffCanEditAServiceDraftWithoutChangingItsStableSlug() throws Exception {
        var service = serviceRepository.findBySlug("interior-painting").orElseThrow();
        service.setStatus(PublicationStatus.DRAFT);
        service.setActive(false);
        service.setPublishedAt(null);
        service = serviceRepository.saveAndFlush(service);

        mockMvc.perform(patch("/api/admin/content/services/{serviceId}", service.getId())
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Premium Interior Painting",
                                  "label": "Interior environments",
                                  "summary": "Careful painting for interior spaces.",
                                  "description": "A detailed interior service planned around the property and its occupants.",
                                  "inclusions": ["Walls and ceilings", "Doors and trim"],
                                  "note": "The final scope is confirmed after assessment.",
                                  "displayOrder": 1,
                                  "version": %d
                                }
                                """.formatted(service.getVersion())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Premium Interior Painting"))
                .andExpect(jsonPath("$.slug").value("interior-painting"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void staffCannotChangeServicePublication() throws Exception {
        var service = serviceRepository.findBySlug("exterior-painting").orElseThrow();

        mockMvc.perform(patch("/api/admin/content/services/{serviceId}/publication", service.getId())
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "DRAFT", "version": %d}
                                """.formatted(service.getVersion())))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanHideAndRepublishAService() throws Exception {
        var service = serviceRepository.findBySlug("commercial-painting").orElseThrow();

        var hidden = mockMvc.perform(patch("/api/admin/content/services/{serviceId}/publication", service.getId())
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "DRAFT", "version": %d}
                                """.formatted(service.getVersion())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        var hiddenVersion = com.jayway.jsonpath.JsonPath.parse(hidden.getResponse().getContentAsString()).read("$.version", Long.class);

        mockMvc.perform(get("/api/services"))
                .andExpect(jsonPath("$[?(@.slug == 'commercial-painting')]").isEmpty());

        mockMvc.perform(patch("/api/admin/content/services/{serviceId}/publication", service.getId())
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PUBLISHED", "version": %d}
                                """.formatted(hiddenVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }
}
