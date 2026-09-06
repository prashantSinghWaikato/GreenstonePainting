package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.AdminRole;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.BlogArticle;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.BlogArticleRepository;
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
class AdminArticleControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository userRepository;

    @Autowired
    private BlogArticleRepository articleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AdminUser owner;
    private AdminUser staff;

    @BeforeEach
    void createTeam() {
        owner = userRepository.saveAndFlush(new AdminUser(
                "article-owner@greenstonepainting.co.nz", passwordEncoder.encode("owner-password-123"),
                "Article Owner", AdminRole.OWNER
        ));
        staff = userRepository.saveAndFlush(new AdminUser(
                "article-staff@greenstonepainting.co.nz", passwordEncoder.encode("staff-password-123"),
                "Article Staff", AdminRole.STAFF
        ));
    }

    @Test
    void staffCanCreatePrivateArticleDrafts() throws Exception {
        mockMvc.perform(post("/api/admin/content/articles")
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Choosing an Exterior Finish",
                                  "shortTitle": "Choosing an Exterior Finish",
                                  "topic": "Materials",
                                  "excerpt": "A guide to selecting a practical coating for Waikato conditions.",
                                  "body": "Start with the substrate and exposure.\\n\\n## Check the surface\\nPreparation determines the result.",
                                  "readTimeMinutes": 4,
                                  "version": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.slug").value("choosing-an-exterior-finish"));
    }

    @Test
    void staffCannotPublishArticles() throws Exception {
        var article = articleRepository.saveAndFlush(article("private-staff-article", "Private Staff Article"));

        mockMvc.perform(patch("/api/admin/content/articles/{articleId}/publication", article.getId())
                        .with(user(staff.getEmail()).roles("ADMIN", "STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PUBLISHED", "version": %d}
                                """.formatted(article.getVersion())))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanPublishAnArticleAfterAFeaturedImageIsAdded() throws Exception {
        var article = article("owner-ready-article", "Owner Ready Article");
        article.setFeaturedImage(
                "static:/images/greenstone-bedroom.webp", "A freshly painted bedroom",
                "greenstone-bedroom.webp", "image/webp", 0
        );
        article = articleRepository.saveAndFlush(article);

        mockMvc.perform(patch("/api/admin/content/articles/{articleId}/publication", article.getId())
                        .with(user(owner.getEmail()).roles("ADMIN", "OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PUBLISHED", "version": %d}
                                """.formatted(article.getVersion())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty());
    }

    @Test
    void publicJournalReturnsPublishedArticlesButNotDrafts() throws Exception {
        articleRepository.saveAndFlush(article("hidden-public-draft", "Hidden Public Draft"));

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'hidden-public-draft')]").isEmpty())
                .andExpect(jsonPath("$[?(@.slug == 'how-painters-prepare-your-home-for-a-smooth-paint-job')]").isNotEmpty());
    }

    private BlogArticle article(String slug, String title) {
        return new BlogArticle(
                slug, title, title, "Preparation", "A useful article summary.",
                "An introduction.\n\n## A section\nPractical guidance.", 4
        );
    }
}
