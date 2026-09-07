package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.ServiceOfferingActivityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminServiceActivityResponse(
        UUID id,
        ServiceOfferingActivityType type,
        String summary,
        String actorDisplayName,
        OffsetDateTime createdAt
) {}
