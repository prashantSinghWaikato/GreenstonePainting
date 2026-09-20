package org.greenstone.backend.publiccontent;

import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class PublicReviewController {

    private final GoogleReviewsService googleReviewsService;

    public PublicReviewController(GoogleReviewsService googleReviewsService) {
        this.googleReviewsService = googleReviewsService;
    }

    @GetMapping
    public ResponseEntity<PublicReviewsResponse> reviews() {
        try {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(googleReviewsService.reviews());
        } catch (GoogleReviewsUnavailableException exception) {
            return ResponseEntity.noContent().build();
        }
    }
}
