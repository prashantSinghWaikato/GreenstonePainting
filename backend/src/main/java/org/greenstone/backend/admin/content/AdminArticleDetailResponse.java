package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.PublicationStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminArticleDetailResponse(
        UUID id,
        String slug,
        String title,
        String shortTitle,
        String topic,
        String excerpt,
        String body,
        int readTimeMinutes,
        PublicationStatus status,
        String imageUrl,
        String imageAlt,
        String imageFilename,
        Long imageSizeBytes,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        long version,
        List<AdminArticleActivityResponse> activities
) {
}
