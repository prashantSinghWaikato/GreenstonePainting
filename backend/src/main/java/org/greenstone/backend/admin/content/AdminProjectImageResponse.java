package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.ProjectImagePhase;

import java.util.UUID;

public record AdminProjectImageResponse(
        UUID id,
        String altText,
        ProjectImagePhase phase,
        int displayOrder,
        String imageUrl,
        String originalFilename,
        String contentType,
        long sizeBytes
) {
}
