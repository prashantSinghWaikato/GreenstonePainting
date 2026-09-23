package org.greenstone.backend;

import com.jayway.jsonpath.JsonPath;
import org.greenstone.backend.persistence.entity.AdminRole;
import org.greenstone.backend.persistence.entity.AdminUser;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminColourControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private AdminUserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private AdminUser owner;
    private AdminUser staff;

    @BeforeEach
    void createTeam() {
        owner = userRepository.saveAndFlush(new AdminUser(
                "colour-owner@greenstonepainting.co.nz", passwordEncoder.encode("owner-password-123"),
                "Colour Owner", AdminRole.OWNER
        ));
        staff = userRepository.saveAndFlush(new AdminUser(
                "colour-staff@greenstonepainting.co.nz", passwordEncoder.encode("staff-password-123"),
                "Colour Staff", AdminRole.STAFF
        ));
    }

    @Test
    void seededPaletteIsPublicAndOrdered() throws Exception {
        mockMvc.perform(get("/api/colours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].name").value("Sea Fog"))
                .andExpect(jsonPath("$[0].defaultInterior").value(true))
                .andExpect(jsonPath("$[4].name").value("Lemon Grass"))
                .andExpect(jsonPath("$[4].defaultExterior").value(true));
    }

    @Test
    void staffCannotManagePalette() throws Exception {
        mockMvc.perform(post("/api/admin/content/colours")
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(colourJson("Test Colour", "#112233", 11, 0, true)))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanAddHideAndRemoveAColour() throws Exception {
        var created = mockMvc.perform(post("/api/admin/content/colours")
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(colourJson("Test Colour", "#112233", 11, 0, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Colour"))
                .andReturn();

        var body = JsonPath.parse(created.getResponse().getContentAsString());
        var id = body.read("$.id", String.class);
        var version = body.read("$.version", Long.class);

        mockMvc.perform(patch("/api/admin/content/colours/{colourId}", id)
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(colourJson("Test Colour", "#112233", 11, version, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/colours"))
                .andExpect(jsonPath("$[?(@.name == 'Test Colour')]").isEmpty());

        mockMvc.perform(delete("/api/admin/content/colours/{colourId}", id)
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    private String colourJson(String name, String hex, int order, long version, boolean active) {
        return """
                {
                  "name": "%s",
                  "hex": "%s",
                  "reseneUrl": "https://www.resene.co.nz/colours/",
                  "active": %s,
                  "displayOrder": %d,
                  "defaultInterior": false,
                  "defaultExterior": false,
                  "version": %d
                }
                """.formatted(name, hex, active, order, version);
    }
}
