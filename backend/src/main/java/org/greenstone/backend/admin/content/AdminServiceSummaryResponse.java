package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.PublicationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminServiceSummaryResponse(
        UUID id,
        String slug,
        String title,
        String label,
        int displayOrder,
        PublicationStatus status,
        String imageUrl,
        OffsetDateTime updatedAt,
        long version
) {}
