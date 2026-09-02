package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.AdminRole;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.PortfolioProject;
import org.greenstone.backend.persistence.entity.ProjectImage;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.PortfolioProjectRepository;
import org.greenstone.backend.persistence.repository.ProjectImageRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminProjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository userRepository;

    @Autowired
    private PortfolioProjectRepository projectRepository;

    @Autowired
    private ProjectImageRepository imageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AdminUser owner;
    private AdminUser staff;

    @BeforeEach
    void createTeam() {
        owner = userRepository.saveAndFlush(new AdminUser(
                "project-owner@greenstonepainting.co.nz", passwordEncoder.encode("owner-password-123"),
                "Project Owner", AdminRole.OWNER
        ));
        staff = userRepository.saveAndFlush(new AdminUser(
                "project-staff@greenstonepainting.co.nz", passwordEncoder.encode("staff-password-123"),
                "Project Staff", AdminRole.STAFF
        ));
    }

    @Test
    void staffCanCreatePrivateProjectDrafts() throws Exception {
        mockMvc.perform(post("/api/admin/content/projects")
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cambridge Villa Refresh",
                                  "summary": "A detailed repaint for a character home.",
                                  "description": "Careful preparation\\nInterior and exterior finish",
                                  "location": "Cambridge, Waikato",
                                  "completedOn": null,
                                  "serviceSlug": "exterior-painting",
                                  "featured": false,
                                  "version": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.slug").value("cambridge-villa-refresh"));
    }

    @Test
    void staffCannotPublishProjects() throws Exception {
        var project = projectRepository.saveAndFlush(new PortfolioProject(
                "private-staff-draft", "Private Staff Draft", "Summary", "Highlight"
        ));

        mockMvc.perform(patch("/api/admin/content/projects/{projectId}/publication", project.getId())
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PUBLISHED", "version": %d}
                                """.formatted(project.getVersion())))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanPublishAProjectAfterAnImageIsAdded() throws Exception {
        var project = projectRepository.saveAndFlush(new PortfolioProject(
                "owner-ready-project", "Owner Ready Project", "Summary", "Highlight"
        ));
        var image = new ProjectImage(
                project, "static:/images/greenstone-bedroom.webp", "Painted interior",
                "greenstone-bedroom.webp", "image/webp", 0
        );
        imageRepository.saveAndFlush(image);

        mockMvc.perform(patch("/api/admin/content/projects/{projectId}/publication", project.getId())
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PUBLISHED", "version": %d}
                                """.formatted(project.getVersion())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty());
    }

    @Test
    void publicPortfolioReturnsPublishedProjectsButNotDrafts() throws Exception {
        projectRepository.saveAndFlush(new PortfolioProject(
                "hidden-public-draft", "Hidden Public Draft", "Summary", "Highlight"
        ));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'hidden-public-draft')]").isEmpty())
                .andExpect(jsonPath("$[?(@.slug == 'contemporary-exterior-renewal')]").isNotEmpty());
    }
}
