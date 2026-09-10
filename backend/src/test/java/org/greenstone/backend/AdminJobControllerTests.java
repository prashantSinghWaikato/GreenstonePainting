package org.greenstone.backend;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.greenstone.backend.persistence.entity.AdminRole;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryType;
import org.greenstone.backend.persistence.entity.Quote;
import org.greenstone.backend.persistence.entity.QuoteStatus;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.JobActivityRepository;
import org.greenstone.backend.persistence.repository.PaintingJobRepository;
import org.greenstone.backend.persistence.repository.QuoteRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminJobControllerTests {

    @TempDir
    static Path uploadDirectory;

    @DynamicPropertySource
    static void uploadProperties(DynamicPropertyRegistry registry) {
        registry.add("app.upload.directory", () -> uploadDirectory.toString());
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AdminUserRepository adminUserRepository;
    @Autowired EnquiryRepository enquiryRepository;
    @Autowired QuoteRepository quoteRepository;
    @Autowired ServiceOfferingRepository serviceOfferingRepository;
    @Autowired PaintingJobRepository jobRepository;
    @Autowired JobActivityRepository activityRepository;

    private AdminUser manager;
    private AdminUser painter;

    @BeforeEach
    void createStaff() {
        manager = adminUserRepository.save(new AdminUser(
                "jobs@greenstonepainting.co.nz", "test-password-hash", "Job Manager"
        ));
        painter = adminUserRepository.save(new AdminUser(
                "painter@greenstonepainting.co.nz", "test-password-hash", "Ari Painter", AdminRole.STAFF
        ));
    }

    @Test
    void convertsOnlyAcceptedQuotesAndReturnsTheExistingJobOnRepeat() throws Exception {
        var accepted = quote(QuoteStatus.ACCEPTED, true);

        var first = createJob(accepted.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.customerName").value("Aroha Williams"))
                .andExpect(jsonPath("$.assignedDisplayName").value("Ari Painter"))
                .andExpect(jsonPath("$.activities", hasSize(1)))
                .andReturn().getResponse().getContentAsString();
        var firstId = objectMapper.readTree(first).get("id").asText();

        createJob(accepted.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId));

        assertThat(jobRepository.count()).isEqualTo(1);
        assertThat(activityRepository.count()).isEqualTo(1);

        var draft = quote(QuoteStatus.DRAFT, false);
        createJob(draft.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only an accepted quote can be converted into a job."));

        mockMvc.perform(get("/api/admin/jobs").with(staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.metrics.scheduled").value(1));
        mockMvc.perform(get("/api/admin/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatesWorkflowRecordsHistoryAndRejectsStaleOrTerminalTransitions() throws Exception {
        var created = objectMapper.readTree(createJob(quote(QuoteStatus.ACCEPTED, false).getId())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        var id = created.get("id").asText();
        var version = created.get("version").asLong();
        var start = LocalDate.now().plusDays(7);
        var end = start.plusDays(4);

        var inProgress = patchJob(id, version, "IN_PROGRESS", painter.getId(), start, end)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assignedDisplayName").value("Ari Painter"))
                .andExpect(jsonPath("$.siteInstructions").value("Use the side entrance."))
                .andExpect(jsonPath("$.actualStartedAt").isNotEmpty())
                .andExpect(jsonPath("$.activities.length()").value(4))
                .andReturn().getResponse().getContentAsString();
        var currentVersion = objectMapper.readTree(inProgress).get("version").asLong();

        patchJob(id, version, "ON_HOLD", painter.getId(), start, end)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This job was updated by another staff member. Reload it before saving again."));

        patchJob(id, currentVersion, "COMPLETED", painter.getId(), start, end)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Complete the site checklist and record customer sign-off before completing this job."));

        for (var item : objectMapper.readTree(inProgress).get("checklist")) {
            mockMvc.perform(patch("/api/admin/jobs/{jobId}/checklist/{itemId}", id, item.get("id").asText())
                            .with(staff()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"completed\":true}"))
                    .andExpect(status().isOk());
        }
        var signedOff = mockMvc.perform(post("/api/admin/jobs/{id}/signoff", id)
                        .with(staff()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Aroha Williams\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerSignoffName").value("Aroha Williams"))
                .andReturn().getResponse().getContentAsString();
        currentVersion = objectMapper.readTree(signedOff).get("version").asLong();

        var completed = patchJob(id, currentVersion, "COMPLETED", painter.getId(), start, end)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        var completedVersion = objectMapper.readTree(completed).get("version").asLong();

        patchJob(id, completedVersion, "IN_PROGRESS", painter.getId(), start, end)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Completed or cancelled jobs cannot be reopened."));

        var invoiceBody = mockMvc.perform(post("/api/admin/jobs/{id}/invoice", id).with(staff()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.total").value(11500.0))
                .andReturn().getResponse().getContentAsString();
        var invoice = objectMapper.readTree(invoiceBody);
        var invoiceId = invoice.get("id").asText();
        mockMvc.perform(get("/api/admin/invoices/{id}/pdf", invoiceId).with(staff()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).startsWith("%PDF".getBytes()));
        mockMvc.perform(patch("/api/admin/invoices/{id}", invoiceId).with(staff()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"SENT","amountPaid":500,"dueDate":"%s","paymentReference":"DEP-100","notes":"Deposit received.","version":%d}
                                """.formatted(LocalDate.now().plusDays(14), invoice.get("version").asLong())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PART_PAID"))
                .andExpect(jsonPath("$.balanceDue").value(11000.0));
        mockMvc.perform(get("/api/admin/reports/summary").with(staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedJobs").value(1))
                .andExpect(jsonPath("$.collectedRevenue").value(500.0))
                .andExpect(jsonPath("$.outstandingRevenue").value(11000.0));
    }

    @Test
    void storesAndProtectsJobPhotos() throws Exception {
        var created = objectMapper.readTree(createJob(quote(QuoteStatus.ACCEPTED, false).getId())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        var jobId = created.get("id").asText();
        var png = new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        var file = new MockMultipartFile("file", "before.png", "image/png", png);

        var response = mockMvc.perform(multipart("/api/admin/jobs/{id}/photos", jobId)
                        .file(file).param("phase", "BEFORE").with(staff()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photos", hasSize(1)))
                .andExpect(jsonPath("$.photos[0].phase").value("BEFORE"))
                .andExpect(jsonPath("$.activities[0].type").value("PHOTO_ADDED"))
                .andReturn().getResponse().getContentAsString();
        JsonNode photo = objectMapper.readTree(response).get("photos").get(0);

        mockMvc.perform(get("/api/admin/jobs/{jobId}/photos/{photoId}", jobId, photo.get("id").asText()).with(staff()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Cache-Control", "private, no-store"))
                .andExpect(content().bytes(png));
        mockMvc.perform(get("/api/admin/jobs/{jobId}/photos/{photoId}", jobId, photo.get("id").asText()))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions createJob(UUID quoteId) throws Exception {
        return mockMvc.perform(post("/api/admin/quotes/{quoteId}/job", quoteId).with(staff()).with(csrf()));
    }

    private org.springframework.test.web.servlet.ResultActions patchJob(
            String jobId, long version, String status, UUID assignedAdminId, LocalDate start, LocalDate end
    ) throws Exception {
        var body = """
                {
                  "status":"%s",
                  "assignedAdminId":"%s",
                  "scheduledStartDate":"%s",
                  "scheduledEndDate":"%s",
                  "siteInstructions":"Use the side entrance.",
                  "internalNotes":"Customer prefers morning updates.",
                  "version":%d
                }
                """.formatted(status, assignedAdminId, start, end, version);
        return mockMvc.perform(patch("/api/admin/jobs/{id}", jobId)
                .with(staff()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private Quote quote(QuoteStatus status, boolean scheduled) {
        var enquiry = new Enquiry(
                EnquiryType.QUOTE_REQUEST, "Aroha", "Williams", "aroha@example.com",
                "Prepare and paint the exterior weatherboards."
        );
        enquiry.setPhone("021 555 0100");
        enquiry.setPropertyAddress("29 Example Street, Hamilton");
        enquiry.setService(serviceOfferingRepository.findBySlug("exterior-painting").orElseThrow());
        enquiry.setAssignedTo(painter);
        enquiryRepository.saveAndFlush(enquiry);

        var quote = new Quote(enquiry, manager, "GST-" + UUID.randomUUID().toString().substring(0, 8), 1);
        quote.setStatus(status);
        quote.setCustomerName("Aroha Williams");
        quote.setCustomerEmail("aroha@example.com");
        quote.setPropertyAddress("29 Example Street, Hamilton");
        quote.setTitle("Exterior repaint");
        quote.setScope("Wash, prepare, prime and apply two exterior top coats.");
        quote.setTerms("Colours confirmed before work begins.");
        quote.setSubtotal(new BigDecimal("10000.00"));
        quote.setGstAmount(new BigDecimal("1500.00"));
        quote.setTotal(new BigDecimal("11500.00"));
        quote.setValidUntil(LocalDate.now().plusDays(30));
        if (scheduled) {
            quote.setEstimatedStartDate(LocalDate.now().plusDays(14));
            quote.setEstimatedEndDate(LocalDate.now().plusDays(19));
        }
        return quoteRepository.saveAndFlush(quote);
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor staff() {
        return user("jobs@greenstonepainting.co.nz").roles("ADMIN");
    }
}
