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

@RestController
@RequestMapping("/api/articles")
public class PublicArticleController {

    private final PublicArticleService articleService;

    public PublicArticleController(PublicArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<PublicArticleResponse> articles() { return articleService.articles(); }

    @GetMapping("/{slug}")
    public PublicArticleResponse article(@PathVariable String slug) { return articleService.article(slug); }

    @GetMapping("/{slug}/image")
    public ResponseEntity<org.springframework.core.io.Resource> image(@PathVariable String slug) {
        var download = articleService.image(slug);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.filename(), StandardCharsets.UTF_8).build().toString())
                .body(download.resource());
    }
}
