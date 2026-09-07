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
@RequestMapping("/api/services")
public class PublicServiceContentController {

    private final PublicServiceContentService serviceContent;

    public PublicServiceContentController(PublicServiceContentService serviceContent) {
        this.serviceContent = serviceContent;
    }

    @GetMapping
    public List<PublicServiceResponse> services() { return serviceContent.services(); }

    @GetMapping("/{slug}")
    public PublicServiceResponse service(@PathVariable String slug) { return serviceContent.service(slug); }

    @GetMapping("/{slug}/image")
    public ResponseEntity<org.springframework.core.io.Resource> image(@PathVariable String slug) {
        var download = serviceContent.image(slug);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.filename(), StandardCharsets.UTF_8).build().toString())
                .body(download.resource());
    }
}
