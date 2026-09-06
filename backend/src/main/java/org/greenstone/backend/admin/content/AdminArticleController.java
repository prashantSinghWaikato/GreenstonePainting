package org.greenstone.backend.admin.content;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/content/articles")
public class AdminArticleController {

    private final AdminArticleService articleService;

    public AdminArticleController(AdminArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<AdminArticleSummaryResponse> list() { return articleService.list(); }

    @GetMapping("/{articleId}")
    public AdminArticleDetailResponse detail(@PathVariable UUID articleId) { return articleService.detail(articleId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminArticleDetailResponse create(
            @Valid @RequestBody SaveAdminArticleRequest request,
            Authentication authentication
    ) {
        return articleService.create(request, authentication.getName());
    }

    @PatchMapping("/{articleId}")
    public AdminArticleDetailResponse update(
            @PathVariable UUID articleId,
            @Valid @RequestBody SaveAdminArticleRequest request,
            Authentication authentication
    ) {
        return articleService.update(articleId, request, authentication.getName());
    }

    @PatchMapping("/{articleId}/publication")
    public AdminArticleDetailResponse publication(
            @PathVariable UUID articleId,
            @Valid @RequestBody SetProjectPublicationRequest request,
            Authentication authentication
    ) {
        return articleService.setPublication(articleId, request, authentication.getName());
    }

    @PostMapping(value = "/{articleId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminArticleDetailResponse setImage(
            @PathVariable UUID articleId,
            @RequestPart("file") MultipartFile file,
            @RequestParam String altText,
            Authentication authentication
    ) {
        return articleService.setImage(articleId, file, altText, authentication.getName());
    }

    @DeleteMapping("/{articleId}/image")
    public AdminArticleDetailResponse removeImage(
            @PathVariable UUID articleId,
            Authentication authentication
    ) {
        return articleService.removeImage(articleId, authentication.getName());
    }

    @GetMapping("/{articleId}/image/file")
    public ResponseEntity<org.springframework.core.io.Resource> image(@PathVariable UUID articleId) {
        var download = articleService.loadImage(articleId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.filename(), StandardCharsets.UTF_8).build().toString())
                .body(download.resource());
    }
}
