package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.PublicationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminProjectSummaryResponse(
        UUID id,
        String slug,
        String title,
        String location,
        String serviceTitle,
        PublicationStatus status,
        boolean featured,
        String imageUrl,
        OffsetDateTime updatedAt,
        long version
) {
}
