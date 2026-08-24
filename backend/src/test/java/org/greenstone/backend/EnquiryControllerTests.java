package org.greenstone.backend;

import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.EnquiryAttachmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EnquiryControllerTests {

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

    @Test
    void createsAValidatedQuoteEnquiry() throws Exception {
        var initialCount = enquiryRepository.countByStatus(EnquiryStatus.NEW);

        mockMvc.perform(post("/api/enquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Aroha",
                                  "lastName": "Williams",
                                  "email": "aroha@example.com",
                                  "phone": "021 555 0100",
                                  "serviceSlug": "interior-painting",
                                  "propertyAddress": "29 Example Street, Hamilton",
                                  "message": "Please quote repainting a three-bedroom home."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/enquiries/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertThat(enquiryRepository.countByStatus(EnquiryStatus.NEW)).isEqualTo(initialCount + 1);
    }

    @Test
    void rejectsInvalidQuoteEnquiry() throws Exception {
        mockMvc.perform(post("/api/enquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "",
                                  "email": "not-an-email",
                                  "phone": "",
                                  "serviceSlug": "",
                                  "propertyAddress": "",
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please check the highlighted fields."))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }

    @Test
    void rejectsAnInvalidPhoneNumberWithAClearMessage() throws Exception {
        mockMvc.perform(post("/api/enquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Aroha",
                                  "lastName": "Williams",
                                  "email": "aroha@example.com",
                                  "phone": "call me later",
                                  "serviceSlug": "interior-painting",
                                  "propertyAddress": "29 Example Street, Hamilton",
                                  "message": "Please provide a quote."
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.phone").value("Enter a valid phone number using 7 to 15 digits."));
    }

    @Test
    void rejectsUnavailableService() throws Exception {
        mockMvc.perform(post("/api/enquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Aroha",
                                  "lastName": "Williams",
                                  "email": "aroha@example.com",
                                  "phone": "021 555 0100",
                                  "serviceSlug": "unknown-service",
                                  "propertyAddress": "29 Example Street, Hamilton",
                                  "message": "Please provide a quote."
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Selected service is not available."));
    }

    @Test
    void uploadsNoMoreThanFourPhotosWithTheEnquiryUploadToken() throws Exception {
        var created = createQuoteEnquiry();
        var enquiryId = jsonString(created, "id");
        var uploadToken = jsonString(created, "uploadToken");

        for (int index = 1; index <= 4; index++) {
            var photo = new MockMultipartFile(
                    "file",
                    "project-" + index + ".jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) index}
            );
            mockMvc.perform(multipart("/api/enquiries/{enquiryId}/attachments", enquiryId)
                            .file(photo)
                            .header("X-Upload-Token", uploadToken))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.originalFilename").value("project-" + index + ".jpg"))
                    .andExpect(jsonPath("$.contentType").value(MediaType.IMAGE_JPEG_VALUE));
        }

        var fifthPhoto = new MockMultipartFile(
                "file", "project-5.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3}
        );
        mockMvc.perform(multipart("/api/enquiries/{enquiryId}/attachments", enquiryId)
                        .file(fifthPhoto)
                        .header("X-Upload-Token", uploadToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A quote request can include up to 4 photos."));

        assertThat(attachmentRepository.countByEnquiryId(java.util.UUID.fromString(enquiryId))).isEqualTo(4);
    }

    @Test
    void rejectsPhotoUploadWithoutTheEnquiryUploadToken() throws Exception {
        var created = createQuoteEnquiry();
        var enquiryId = jsonString(created, "id");
        var photo = new MockMultipartFile(
                "file", "project.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/enquiries/{enquiryId}/attachments", enquiryId)
                        .file(photo)
                        .header("X-Upload-Token", "incorrect-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Photo upload access is invalid or has expired."));
    }

    @Test
    void rejectsUnsupportedPhotoTypes() throws Exception {
        var created = createQuoteEnquiry();
        var enquiryId = jsonString(created, "id");
        var uploadToken = jsonString(created, "uploadToken");
        var document = new MockMultipartFile(
                "file", "scope.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/enquiries/{enquiryId}/attachments", enquiryId)
                        .file(document)
                        .header("X-Upload-Token", uploadToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Use a JPEG, PNG, WebP, HEIC, or HEIF photo."));
    }

    @Test
    void rejectsPhotosLargerThanFiveMegabytes() throws Exception {
        var created = createQuoteEnquiry();
        var enquiryId = jsonString(created, "id");
        var uploadToken = jsonString(created, "uploadToken");
        var oversizedPhoto = new MockMultipartFile(
                "file", "large-project.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[(5 * 1024 * 1024) + 1]
        );

        mockMvc.perform(multipart("/api/enquiries/{enquiryId}/attachments", enquiryId)
                        .file(oversizedPhoto)
                        .header("X-Upload-Token", uploadToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Each photo must be 5 MB or smaller."));
    }

    private String createQuoteEnquiry() throws Exception {
        return mockMvc.perform(post("/api/enquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Aroha",
                                  "lastName": "Williams",
                                  "email": "aroha@example.com",
                                  "phone": "021 555 0100",
                                  "serviceSlug": "interior-painting",
                                  "propertyAddress": "29 Example Street, Hamilton",
                                  "message": "Please quote repainting a three-bedroom home."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uploadToken").isNotEmpty())
                .andExpect(jsonPath("$.uploadExpiresAt").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String jsonString(String json, String field) {
        var matcher = Pattern.compile("\\\"" + field + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("Response did not contain " + field);
        }
        return matcher.group(1);
    }
}
