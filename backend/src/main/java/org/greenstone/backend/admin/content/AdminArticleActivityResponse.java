package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.BlogArticleActivityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminArticleActivityResponse(
        UUID id,
        BlogArticleActivityType type,
        String summary,
        String actorDisplayName,
        OffsetDateTime createdAt
) {
}
