package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.PublicationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminArticleSummaryResponse(
        UUID id,
        String slug,
        String title,
        String topic,
        PublicationStatus status,
        String imageUrl,
        OffsetDateTime publishedAt,
        OffsetDateTime updatedAt,
        long version
) {
}
