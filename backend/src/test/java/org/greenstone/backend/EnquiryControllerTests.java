package org.greenstone.backend;

import org.greenstone.backend.notification.EnquiryNotifier;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EnquiryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EnquiryRepository enquiryRepository;

    @MockitoBean
    private EnquiryNotifier enquiryNotifier;

    @Test
    void createsAValidatedQuoteEnquiryAndNotifiesTheTeam() throws Exception {
        var initialCount = enquiryRepository.countByStatus(EnquiryStatus.NEW);

        mockMvc.perform(post("/api/enquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("interior-painting")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/enquiries/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.notificationSent").value(true));

        assertThat(enquiryRepository.countByStatus(EnquiryStatus.NEW)).isEqualTo(initialCount + 1);
        verify(enquiryNotifier).sendNewQuoteNotification(any(Enquiry.class));
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
                        .content(validRequest("unknown-service")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Selected service is not available."));
    }

    private String validRequest(String serviceSlug) {
        return """
                {
                  "firstName": "Aroha",
                  "lastName": "Williams",
                  "email": "aroha@example.com",
                  "phone": "021 555 0100",
                  "serviceSlug": "%s",
                  "propertyAddress": "29 Example Street, Hamilton",
                  "message": "Please quote repainting a three-bedroom home."
                }
                """.formatted(serviceSlug);
    }
}
