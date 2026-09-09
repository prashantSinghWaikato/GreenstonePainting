package org.greenstone.backend.admin.quote;

import org.greenstone.backend.persistence.entity.QuoteItemCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record QuoteItemResponse(
        UUID id,
        QuoteItemCategory category,
        String description,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        boolean optional
) {
}
