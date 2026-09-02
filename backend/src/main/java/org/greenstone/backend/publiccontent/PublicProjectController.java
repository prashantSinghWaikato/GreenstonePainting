package org.greenstone.backend.publiccontent;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class PublicProjectController {

    private final PublicProjectService projectService;

    public PublicProjectController(PublicProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<PublicProjectResponse> projects() {
        return projectService.publishedProjects();
    }

    @GetMapping("/{slug}/images/{imageId}")
    public ResponseEntity<org.springframework.core.io.Resource> image(
            @PathVariable String slug,
            @PathVariable UUID imageId
    ) {
        var download = projectService.loadPublishedImage(slug, imageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.filename(), StandardCharsets.UTF_8).build().toString())
                .body(download.resource());
    }
}
