package org.greenstone.backend.admin.quote;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quotes")
public class PublicQuoteController {

    private final AdminQuoteService service;

    public PublicQuoteController(AdminQuoteService service) {
        this.service = service;
    }

    @GetMapping("/response")
    public PublicQuoteResponse find(@RequestParam String token) {
        return service.publicFind(token);
    }

    @PostMapping("/response")
    public PublicQuoteResponse respond(@RequestParam String token, @Valid @RequestBody QuoteDecisionRequest request) {
        return service.respond(token, request);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam String token) {
        var body = service.publicPdf(token);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(body.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("greenstone-quote.pdf").build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(body);
    }
}
