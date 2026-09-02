package org.greenstone.backend.admin.enquiry;

import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/enquiries")
public class AdminEnquiryController {

    private final AdminEnquiryService adminEnquiryService;

    public AdminEnquiryController(AdminEnquiryService adminEnquiryService) {
        this.adminEnquiryService = adminEnquiryService;
    }

    @GetMapping
    public AdminEnquiryPageResponse search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) EnquiryStatus status,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminEnquiryService.search(q, status, service, from, to, page, size);
    }

    @GetMapping("/{enquiryId}")
    public AdminEnquiryDetailResponse find(@PathVariable UUID enquiryId) {
        return adminEnquiryService.find(enquiryId);
    }

    @PatchMapping("/{enquiryId}/workflow")
    public AdminEnquiryDetailResponse updateWorkflow(
            @PathVariable UUID enquiryId,
            @Valid @RequestBody UpdateEnquiryWorkflowRequest request,
            Authentication authentication
    ) {
        return adminEnquiryService.updateWorkflow(enquiryId, request, authentication.getName());
    }

    @GetMapping("/{enquiryId}/attachments/{attachmentId}")
    public ResponseEntity<org.springframework.core.io.Resource> viewAttachment(
            @PathVariable UUID enquiryId,
            @PathVariable UUID attachmentId
    ) {
        var download = adminEnquiryService.downloadAttachment(enquiryId, attachmentId);
        var disposition = ContentDisposition.inline()
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
