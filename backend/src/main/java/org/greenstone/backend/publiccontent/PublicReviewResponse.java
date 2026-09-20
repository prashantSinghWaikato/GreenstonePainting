package org.greenstone.backend.publiccontent;

public record PublicReviewResponse(
        String authorName,
        String authorUri,
        String authorPhotoUri,
        double rating,
        String text,
        String relativePublishTime
) {
}
