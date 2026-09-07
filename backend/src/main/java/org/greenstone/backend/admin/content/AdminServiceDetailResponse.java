package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.PublicationStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminServiceDetailResponse(
        UUID id,
        String slug,
        String title,
        String label,
        String summary,
        String description,
        List<String> inclusions,
        String note,
        int displayOrder,
        PublicationStatus status,
        String imageUrl,
        String imageAlt,
        String imageFilename,
        Long imageSizeBytes,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        long version,
        List<AdminServiceActivityResponse> activities
) {}
