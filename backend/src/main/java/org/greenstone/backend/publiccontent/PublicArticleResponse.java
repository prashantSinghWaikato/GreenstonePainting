package org.greenstone.backend.publiccontent;

import java.time.OffsetDateTime;

public record PublicArticleResponse(
        String slug,
        String path,
        String title,
        String shortTitle,
        String topic,
        String excerpt,
        String body,
        int readTimeMinutes,
        String imageUrl,
        String imageAlt,
        OffsetDateTime publishedAt
) {
}
