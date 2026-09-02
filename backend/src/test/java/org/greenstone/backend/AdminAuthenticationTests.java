package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminAuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createAdmin() {
        adminUserRepository.save(new AdminUser(
                "office@greenstonepainting.co.nz",
                passwordEncoder.encode("a-secure-test-password"),
                "Greenstone Office"
        ));
    }

    @Test
    void protectsTheAdminSessionEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Staff authentication is required."));
    }

    @Test
    void signsInAndPersistsAStaffSession() throws Exception {
        var result = mockMvc.perform(post("/api/admin/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "office@greenstonepainting.co.nz",
                                  "password": "a-secure-test-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Greenstone Office"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andReturn();

        var session = (MockHttpSession) result.getRequest().getSession(false);
        mockMvc.perform(get("/api/admin/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("office@greenstonepainting.co.nz"));
    }

    @Test
    void supportsTheBrowserCsrfHandshakeThroughTheProtectedSession() throws Exception {
        var csrfResult = mockMvc.perform(get("/api/admin/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        var csrfToken = com.jayway.jsonpath.JsonPath.read(
                csrfResult.getResponse().getContentAsString(),
                "$.token"
        ).toString();
        var csrfHeader = com.jayway.jsonpath.JsonPath.read(
                csrfResult.getResponse().getContentAsString(),
                "$.headerName"
        ).toString();
        var csrfSession = (MockHttpSession) csrfResult.getRequest().getSession(false);

        org.assertj.core.api.Assertions.assertThat(csrfSession).isNotNull();

        mockMvc.perform(post("/api/admin/auth/login")
                        .session(csrfSession)
                        .header(csrfHeader, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "office@greenstonepainting.co.nz",
                                  "password": "a-secure-test-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void rejectsIncorrectCredentialsWithoutRevealingWhichFieldFailed() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "office@greenstonepainting.co.nz",
                                  "password": "incorrect-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email or password is incorrect."));
    }

    @Test
    void temporarilyLocksAnAccountAfterFiveFailedSignInAttempts() throws Exception {
        for (int attempt = 1; attempt <= 4; attempt++) {
            mockMvc.perform(post("/api/admin/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "office@greenstonepainting.co.nz",
                                      "password": "incorrect-password"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/admin/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "office@greenstonepainting.co.nz",
                                  "password": "incorrect-password"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many unsuccessful sign-in attempts. Try again in 15 minutes."));

        assertThat(adminUserRepository.findByEmailIgnoreCase("office@greenstonepainting.co.nz").orElseThrow().isLocked())
                .isTrue();
    }

    @Test
    void rejectsLoginWithoutACsrfToken() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "office@greenstonepainting.co.nz",
                                  "password": "a-secure-test-password"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
