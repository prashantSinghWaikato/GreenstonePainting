package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.ProjectActivityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminProjectActivityResponse(
        UUID id,
        ProjectActivityType type,
        String summary,
        String actorDisplayName,
        OffsetDateTime createdAt
) {
}
