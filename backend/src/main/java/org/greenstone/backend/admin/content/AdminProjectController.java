package org.greenstone.backend.admin.content;

import jakarta.validation.Valid;
import org.greenstone.backend.persistence.entity.ProjectImagePhase;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/content/projects")
public class AdminProjectController {

    private final AdminProjectService projectService;

    public AdminProjectController(AdminProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public AdminProjectDashboardResponse dashboard() {
        return projectService.dashboard();
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public AdminProjectDetailResponse create(
            @Valid @RequestBody SaveAdminProjectRequest request,
            Authentication authentication
    ) {
        return projectService.create(request, authentication.getName());
    }

    @GetMapping("/{projectId}")
    public AdminProjectDetailResponse detail(@PathVariable UUID projectId) {
        return projectService.detail(projectId);
    }

    @PatchMapping("/{projectId}")
    public AdminProjectDetailResponse update(
            @PathVariable UUID projectId,
            @Valid @RequestBody SaveAdminProjectRequest request,
            Authentication authentication
    ) {
        return projectService.update(projectId, request, authentication.getName());
    }

    @PatchMapping("/{projectId}/publication")
    public AdminProjectDetailResponse publication(
            @PathVariable UUID projectId,
            @Valid @RequestBody SetProjectPublicationRequest request,
            Authentication authentication
    ) {
        return projectService.setPublication(projectId, request, authentication.getName());
    }

    @PostMapping(value = "/{projectId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminProjectDetailResponse addImage(
            @PathVariable UUID projectId,
            @RequestPart("file") MultipartFile file,
            @RequestParam String altText,
            @RequestParam ProjectImagePhase phase,
            Authentication authentication
    ) {
        return projectService.addImage(projectId, file, altText, phase, authentication.getName());
    }

    @DeleteMapping("/{projectId}/images/{imageId}")
    public AdminProjectDetailResponse removeImage(
            @PathVariable UUID projectId,
            @PathVariable UUID imageId,
            Authentication authentication
    ) {
        return projectService.removeImage(projectId, imageId, authentication.getName());
    }

    @GetMapping("/{projectId}/images/{imageId}/file")
    public ResponseEntity<org.springframework.core.io.Resource> image(
            @PathVariable UUID projectId,
            @PathVariable UUID imageId
    ) {
        var download = projectService.loadImage(projectId, imageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.filename(), StandardCharsets.UTF_8).build().toString())
                .body(download.resource());
    }
}
