package org.greenstone.backend.admin.quote;

import org.greenstone.backend.persistence.entity.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminQuoteSummaryResponse(
        UUID id,
        String quoteNumber,
        int revisionNumber,
        QuoteStatus status,
        BigDecimal total,
        LocalDate validUntil,
        OffsetDateTime updatedAt,
        OffsetDateTime sentAt
) {
}
