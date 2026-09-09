package org.greenstone.backend;

import tools.jackson.databind.ObjectMapper;
import org.greenstone.backend.admin.quote.QuoteNotifier;
import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.entity.EnquiryType;
import org.greenstone.backend.persistence.entity.Quote;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.greenstone.backend.persistence.repository.EnquiryRepository;
import org.greenstone.backend.persistence.repository.ServiceOfferingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminQuoteControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AdminUserRepository adminUserRepository;
    @Autowired EnquiryRepository enquiryRepository;
    @Autowired ServiceOfferingRepository serviceOfferingRepository;

    @MockitoBean QuoteNotifier quoteNotifier;

    private Enquiry enquiry;

    @BeforeEach
    void createFixture() {
        adminUserRepository.save(new AdminUser(
                "quotes@greenstonepainting.co.nz", "test-password-hash", "Quote Manager"
        ));
        enquiry = new Enquiry(
                EnquiryType.QUOTE_REQUEST, "Aroha", "Williams", "aroha@example.com",
                "Prepare and paint the interior walls and ceilings."
        );
        enquiry.setPhone("021 555 0100");
        enquiry.setPropertyAddress("29 Example Street, Hamilton");
        enquiry.setService(serviceOfferingRepository.findBySlug("interior-painting").orElseThrow());
        enquiryRepository.saveAndFlush(enquiry);
    }

    @Test
    void createsPricesSendsAndAcceptsAQuote() throws Exception {
        var sent = createPriceAndSendQuote();

        mockMvc.perform(get("/api/quotes/response").param("token", sent.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quoteNumber").value(sent.quoteNumber()))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.total").value(1150.0));

        mockMvc.perform(get("/api/quotes/pdf").param("token", sent.token()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray())
                        .startsWith("%PDF".getBytes()));

        mockMvc.perform(post("/api/quotes/response")
                        .param("token", sent.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"ACCEPT\",\"reason\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(post("/api/quotes/response")
                        .param("token", sent.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"ACCEPT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        assertThat(enquiryRepository.findById(enquiry.getId()).orElseThrow().getStatus())
                .isEqualTo(EnquiryStatus.WON);
    }

    @Test
    void revisionsLockTheOldQuoteButKeepItsCustomerLinkInformative() throws Exception {
        var sent = createPriceAndSendQuote();

        mockMvc.perform(post("/api/quotes/response")
                        .param("token", sent.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"DECLINE\",\"reason\":\"Timing has changed.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));

        mockMvc.perform(post("/api/admin/quotes/{id}/revisions", sent.quoteId())
                        .with(staff()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.revisionNumber").value(2))
                .andExpect(jsonPath("$.items.length()").value(2));

        mockMvc.perform(get("/api/quotes/response").param("token", sent.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUPERSEDED"));

        mockMvc.perform(post("/api/quotes/response")
                        .param("token", sent.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"ACCEPT\"}"))
                .andExpect(status().isConflict());

        assertThat(enquiryRepository.findById(enquiry.getId()).orElseThrow().getStatus())
                .isEqualTo(EnquiryStatus.IN_REVIEW);
    }

    @Test
    void protectsAdminMutationsAndRejectsInvalidPublicTokens() throws Exception {
        mockMvc.perform(post("/api/admin/enquiries/{id}/quotes", enquiry.getId()).with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/enquiries/{id}/quotes", enquiry.getId()).with(staff()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/quotes/response").param("token", "not-a-real-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void permitsTheBrowserPreflightForSavingQuoteDrafts() throws Exception {
        mockMvc.perform(options("/api/admin/quotes/{id}", UUID.randomUUID())
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,x-csrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("PUT")));
    }

    private SentQuote createPriceAndSendQuote() throws Exception {
        var createResponse = mockMvc.perform(post("/api/admin/enquiries/{id}/quotes", enquiry.getId())
                        .with(staff()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readTree(createResponse);
        var quoteId = UUID.fromString(created.get("id").asText());
        var version = created.get("version").asLong();
        var validUntil = LocalDate.now().plusDays(30);

        var updateBody = """
                {
                  "customerName":"Aroha Williams",
                  "customerEmail":"aroha@example.com",
                  "propertyAddress":"29 Example Street, Hamilton",
                  "title":"Interior painting proposal",
                  "scope":"Prepare and paint the interior walls and ceilings.",
                  "terms":"Valid for 30 days. Colours will be confirmed before work starts.",
                  "validUntil":"%s",
                  "estimatedStartDate":null,
                  "estimatedEndDate":null,
                  "items":[
                    {"category":"LABOUR","description":"Preparation and painting","quantity":1,"unit":"project","unitPrice":1000,"optional":false},
                    {"category":"OPTIONAL","description":"Garage walls","quantity":1,"unit":"project","unitPrice":200,"optional":true}
                  ],
                  "version":%d
                }
                """.formatted(validUntil, version);

        var updateResponse = mockMvc.perform(put("/api/admin/quotes/{id}", quoteId)
                        .with(staff()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(1000.0))
                .andExpect(jsonPath("$.gstAmount").value(150.0))
                .andExpect(jsonPath("$.total").value(1150.0))
                .andExpect(jsonPath("$.optionalTotal").value(200.0))
                .andReturn().getResponse().getContentAsString();
        var quoteNumber = objectMapper.readTree(updateResponse).get("quoteNumber").asText();

        mockMvc.perform(get("/api/admin/quotes/{id}/pdf", quoteId).with(staff()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray())
                        .startsWith("%PDF".getBytes()));

        mockMvc.perform(post("/api/admin/quotes/{id}/send", quoteId)
                        .with(staff()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));

        var token = ArgumentCaptor.forClass(String.class);
        verify(quoteNotifier).sendQuote(any(Quote.class), any(byte[].class), token.capture());
        return new SentQuote(quoteId, quoteNumber, token.getValue());
    }

    private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor staff() {
        return user("quotes@greenstonepainting.co.nz").roles("ADMIN");
    }

    private record SentQuote(UUID quoteId, String quoteNumber, String token) {}
}
