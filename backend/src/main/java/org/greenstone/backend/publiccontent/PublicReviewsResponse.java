package org.greenstone.backend.publiccontent;

import java.util.List;

public record PublicReviewsResponse(
        double rating,
        int reviewCount,
        String googleMapsUrl,
        List<PublicReviewResponse> reviews
) {
}
