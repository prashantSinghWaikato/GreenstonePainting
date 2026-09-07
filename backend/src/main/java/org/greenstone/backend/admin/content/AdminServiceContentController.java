package org.greenstone.backend.admin.content;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/content/services")
public class AdminServiceContentController {

    private final AdminServiceContentService serviceContent;

    public AdminServiceContentController(AdminServiceContentService serviceContent) {
        this.serviceContent = serviceContent;
    }

    @GetMapping
    public List<AdminServiceSummaryResponse> list() { return serviceContent.list(); }

    @GetMapping("/{serviceId}")
    public AdminServiceDetailResponse detail(@PathVariable UUID serviceId) { return serviceContent.detail(serviceId); }

    @PatchMapping("/{serviceId}")
    public AdminServiceDetailResponse update(
            @PathVariable UUID serviceId,
            @Valid @RequestBody SaveAdminServiceRequest request,
            Authentication authentication
    ) { return serviceContent.update(serviceId, request, authentication.getName()); }

    @PatchMapping("/{serviceId}/publication")
    public AdminServiceDetailResponse publication(
            @PathVariable UUID serviceId,
            @Valid @RequestBody SetProjectPublicationRequest request,
            Authentication authentication
    ) { return serviceContent.setPublication(serviceId, request, authentication.getName()); }

    @PostMapping(value = "/{serviceId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminServiceDetailResponse setImage(
            @PathVariable UUID serviceId,
            @RequestPart("file") MultipartFile file,
            @RequestParam String altText,
            Authentication authentication
    ) { return serviceContent.setImage(serviceId, file, altText, authentication.getName()); }

    @DeleteMapping("/{serviceId}/image")
    public AdminServiceDetailResponse removeImage(@PathVariable UUID serviceId, Authentication authentication) {
        return serviceContent.removeImage(serviceId, authentication.getName());
    }

    @GetMapping("/{serviceId}/image/file")
    public ResponseEntity<org.springframework.core.io.Resource> image(@PathVariable UUID serviceId) {
        var download = serviceContent.loadImage(serviceId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.filename(), StandardCharsets.UTF_8).build().toString())
                .body(download.resource());
    }
}
