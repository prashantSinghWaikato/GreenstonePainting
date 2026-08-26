package org.greenstone.backend.enquiry;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

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

    @PostMapping("/{enquiryId}/complete")
    public ResponseEntity<EnquiryCompletionResponse> complete(
            @PathVariable UUID enquiryId,
            @RequestHeader("X-Upload-Token") String uploadToken
    ) {
        return ResponseEntity.ok(enquiryService.completeQuoteEnquiry(enquiryId, uploadToken));
    }

    @GetMapping("/{enquiryId}/attachments/{attachmentId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(
            @PathVariable UUID enquiryId,
            @PathVariable UUID attachmentId,
            @RequestParam("token") String reviewToken
    ) {
        var download = enquiryService.downloadAttachment(enquiryId, attachmentId, reviewToken);
        var disposition = ContentDisposition.attachment()
                .filename(download.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(download.resource());
    }
}
