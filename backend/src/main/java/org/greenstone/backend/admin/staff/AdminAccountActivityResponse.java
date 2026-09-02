package org.greenstone.backend.admin.staff;

import org.greenstone.backend.persistence.entity.AdminAccountActivityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminAccountActivityResponse(
        UUID id,
        AdminAccountActivityType type,
        String summary,
        String actorDisplayName,
        String targetDisplayName,
        OffsetDateTime createdAt
) {
}
