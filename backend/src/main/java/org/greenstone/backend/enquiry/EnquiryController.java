package org.greenstone.backend.enquiry;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/enquiries")
public class EnquiryController {

    private final EnquiryService enquiryService;

    public EnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @PostMapping
    public ResponseEntity<EnquiryResponse> create(@Valid @RequestBody CreateEnquiryRequest request) {
        var response = enquiryService.createQuoteEnquiry(request);
        return ResponseEntity.created(URI.create("/api/enquiries/" + response.id())).body(response);
    }
}
