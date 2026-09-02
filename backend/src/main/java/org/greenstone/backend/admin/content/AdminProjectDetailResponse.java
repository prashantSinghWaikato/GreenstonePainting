package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.PublicationStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminProjectDetailResponse(
        UUID id,
        String slug,
        String title,
        String summary,
        String description,
        String location,
        LocalDate completedOn,
        String serviceSlug,
        String serviceTitle,
        PublicationStatus status,
        boolean featured,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        long version,
        List<AdminProjectImageResponse> images,
        List<AdminProjectActivityResponse> activities
) {
}
