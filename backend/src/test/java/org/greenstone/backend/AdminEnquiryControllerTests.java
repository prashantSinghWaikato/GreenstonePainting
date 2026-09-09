package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.entity.EnquiryType;
import org.greenstone.backend.persistence.repository.EnquiryAttachmentRepository;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.EnquiryActivityRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.greenstone.backend.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminEnquiryControllerTests {

    @TempDir
    static Path uploadDirectory;

    @DynamicPropertySource
    static void uploadProperties(DynamicPropertyRegistry registry) {
        registry.add("app.upload.directory", () -> uploadDirectory.toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EnquiryRepository enquiryRepository;

    @Autowired
    private EnquiryAttachmentRepository attachmentRepository;

    @Autowired
    private ServiceOfferingRepository serviceOfferingRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private EnquiryActivityRepository activityRepository;

    private Enquiry newEnquiry;
    private AdminUser officeUser;

    @BeforeEach
    void createEnquiries() {
        officeUser = adminUserRepository.save(new AdminUser(
                "office@greenstonepainting.co.nz",
                "test-password-hash",
                "Greenstone Office"
        ));
        var interior = serviceOfferingRepository.findBySlug("interior-painting").orElseThrow();
        newEnquiry = new Enquiry(
                EnquiryType.QUOTE_REQUEST,
                "Aroha",
                "Williams",
                "aroha@example.com",
                "Please repaint our three-bedroom home."
        );
        newEnquiry.setPhone("021 555 0100");
        newEnquiry.setPropertyAddress("29 Example Street, Hamilton");
        newEnquiry.setService(interior);
        enquiryRepository.saveAndFlush(newEnquiry);

        var contacted = new Enquiry(
                EnquiryType.QUOTE_REQUEST,
                "Wiremu",
                "King",
                "wiremu@example.com",
                "We need an exterior quote."
        );
        contacted.setStatus(EnquiryStatus.CONTACTED);
        contacted.setService(interior);
        enquiryRepository.saveAndFlush(contacted);
    }

    @Test
    void requiresStaffAuthenticationForTheInbox() throws Exception {
        mockMvc.perform(get("/api/admin/enquiries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchesAndFiltersPaginatedEnquiries() throws Exception {
        mockMvc.perform(get("/api/admin/enquiries")
                        .with(user("office@greenstonepainting.co.nz").roles("ADMIN"))
                        .param("q", "Aroha")
                        .param("status", "NEW")
                        .param("service", "interior-painting")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].firstName").value("Aroha"))
                .andExpect(jsonPath("$.items[0].reference").isNotEmpty())
                .andExpect(jsonPath("$.statusCounts.NEW").isNumber())
                .andExpect(jsonPath("$.services[0].slug").isNotEmpty())
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void returnsACompleteReadOnlyEnquiryView() throws Exception {
        mockMvc.perform(get("/api/admin/enquiries/{id}", newEnquiry.getId())
                        .with(user("office@greenstonepainting.co.nz").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Aroha"))
                .andExpect(jsonPath("$.serviceTitle").value("Interior Painting"))
                .andExpect(jsonPath("$.message").value("Please repaint our three-bedroom home."))
                .andExpect(jsonPath("$.version").isNumber())
                .andExpect(jsonPath("$.attachments").isArray())
                .andExpect(jsonPath("$.activities").isArray());
    }

    @Test
    void updatesStatusAndNotesWithAnAttributedActivityTimeline() throws Exception {
        mockMvc.perform(patch("/api/admin/enquiries/{id}/workflow", newEnquiry.getId())
                        .with(user("office@greenstonepainting.co.nz").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_REVIEW",
                                  "assignedAdminId": null,
                                  "priority": "NORMAL",
                                  "followUpAt": null,
                                  "newNote": "Call the customer after 3 pm.",
                                  "version": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.activities.length()").value(2))
                .andExpect(jsonPath("$.activities[0].noteBody").value("Call the customer after 3 pm."))
                .andExpect(jsonPath("$.activities[0].actorDisplayName").value("Greenstone Office"));

        org.assertj.core.api.Assertions.assertThat(
                activityRepository.findAllByEnquiryIdOrderByCreatedAtDesc(newEnquiry.getId())
        ).hasSize(2);
    }

    @Test
    void rejectsAStaleWorkflowUpdateWithoutOverwritingNewerWork() throws Exception {
        mockMvc.perform(patch("/api/admin/enquiries/{id}/workflow", newEnquiry.getId())
                        .with(user("office@greenstonepainting.co.nz").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONTACTED",
                                  "assignedAdminId": null,
                                  "priority": "NORMAL",
                                  "followUpAt": null,
                                  "newNote": null,
                                  "version": 99
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "This enquiry was updated by another staff member. Reload the latest version before saving again."
                ));

        org.assertj.core.api.Assertions.assertThat(enquiryRepository.findById(newEnquiry.getId()).orElseThrow().getStatus())
                .isEqualTo(EnquiryStatus.NEW);
    }

    @Test
    void requiresAuthenticationAndCsrfForWorkflowChanges() throws Exception {
        var body = """
                {"status":"IN_REVIEW","assignedAdminId":null,"priority":"NORMAL","followUpAt":null,"newNote":null,"version":0}
                """;

        mockMvc.perform(patch("/api/admin/enquiries/{id}/workflow", newEnquiry.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/admin/enquiries/{id}/workflow", newEnquiry.getId())
                        .with(user("office@greenstonepainting.co.nz").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignsPrioritisesAndFiltersMyEnquiries() throws Exception {
        mockMvc.perform(patch("/api/admin/enquiries/{id}/workflow", newEnquiry.getId())
                        .with(user("office@greenstonepainting.co.nz").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "NEW",
                                  "assignedAdminId": "%s",
                                  "priority": "HIGH",
                                  "followUpAt": "2026-09-10T09:30:00+12:00",
                                  "newNote": null,
                                  "version": 0
                                }
                                """.formatted(officeUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedAdminId").value(officeUser.getId().toString()))
                .andExpect(jsonPath("$.assignedDisplayName").value("Greenstone Office"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.followUpAt").isNotEmpty())
                .andExpect(jsonPath("$.activities.length()").value(3));

        mockMvc.perform(get("/api/admin/enquiries")
                        .with(user("office@greenstonepainting.co.nz").roles("ADMIN"))
                        .param("assignment", "mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].firstName").value("Aroha"))
                .andExpect(jsonPath("$.metrics.mine").value(1))
                .andExpect(jsonPath("$.staff[0].displayName").value("Greenstone Office"));
    }

    @Test
    void securelyStreamsAnEnquiryPhotoToStaff() throws Exception {
        var photo = new MockMultipartFile(
                "file",
                "project.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 1, 2, 3}
        );
        var stored = fileStorageService.storeEnquiryPhoto(newEnquiry.getId(), photo);
        var attachment = attachmentRepository.saveAndFlush(new EnquiryAttachment(
                newEnquiry,
                stored.objectKey(),
                "project.jpg",
                stored.contentType(),
                stored.sizeBytes()
        ));

        mockMvc.perform(get("/api/admin/enquiries/{enquiryId}/attachments/{attachmentId}",
                        newEnquiry.getId(), attachment.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/enquiries/{enquiryId}/attachments/{attachmentId}",
                        newEnquiry.getId(), attachment.getId())
                        .with(user("office@greenstonepainting.co.nz").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, no-store"))
                .andExpect(content().bytes(photo.getBytes()));
    }
}
