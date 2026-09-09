package org.greenstone.backend.admin.quote;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminQuoteController {

    private final AdminQuoteService service;

    public AdminQuoteController(AdminQuoteService service) {
        this.service = service;
    }

    @GetMapping("/enquiries/{enquiryId}/quotes")
    public List<AdminQuoteSummaryResponse> list(@PathVariable UUID enquiryId) {
        return service.list(enquiryId);
    }

    @PostMapping("/enquiries/{enquiryId}/quotes")
    public AdminQuoteDetailResponse create(@PathVariable UUID enquiryId, Authentication authentication) {
        return service.create(enquiryId, authentication.getName());
    }

    @GetMapping("/quotes/{quoteId}")
    public AdminQuoteDetailResponse find(@PathVariable UUID quoteId) {
        return service.find(quoteId);
    }

    @PutMapping("/quotes/{quoteId}")
    public AdminQuoteDetailResponse update(
            @PathVariable UUID quoteId,
            @Valid @RequestBody SaveQuoteRequest request,
            Authentication authentication
    ) {
        return service.update(quoteId, request, authentication.getName());
    }

    @PostMapping("/quotes/{quoteId}/revisions")
    public AdminQuoteDetailResponse revise(@PathVariable UUID quoteId, Authentication authentication) {
        return service.createRevision(quoteId, authentication.getName());
    }

    @PostMapping("/quotes/{quoteId}/send")
    public AdminQuoteDetailResponse send(@PathVariable UUID quoteId, Authentication authentication) {
        return service.send(quoteId, authentication.getName());
    }

    @GetMapping("/quotes/{quoteId}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID quoteId) {
        var body = service.adminPdf(quoteId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(body.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename("greenstone-quote.pdf").build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(body);
    }
}
