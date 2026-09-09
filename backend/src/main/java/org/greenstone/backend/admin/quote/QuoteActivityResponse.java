package org.greenstone.backend.admin.quote;

import org.greenstone.backend.persistence.entity.QuoteActivityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record QuoteActivityResponse(
        UUID id,
        QuoteActivityType type,
        String summary,
        String actorDisplayName,
        OffsetDateTime createdAt
) {
}
