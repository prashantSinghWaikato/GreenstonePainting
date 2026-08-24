package org.greenstone.backend.enquiry;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;

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

    @PostMapping(path = "/{enquiryId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnquiryAttachmentResponse> uploadAttachment(
            @PathVariable UUID enquiryId,
            @RequestHeader("X-Upload-Token") String uploadToken,
            @RequestPart("file") MultipartFile file
    ) {
        var response = enquiryService.addAttachment(enquiryId, uploadToken, file);
        return ResponseEntity.created(URI.create("/api/enquiries/" + enquiryId + "/attachments/" + response.id())).body(response);
    }
}
