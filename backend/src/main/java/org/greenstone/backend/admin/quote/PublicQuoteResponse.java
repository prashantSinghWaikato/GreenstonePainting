package org.greenstone.backend.admin.quote;

import org.greenstone.backend.persistence.entity.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PublicQuoteResponse(
        String quoteNumber,
        int revisionNumber,
        QuoteStatus status,
        String customerName,
        String propertyAddress,
        String title,
        String scope,
        String terms,
        BigDecimal subtotal,
        BigDecimal gstAmount,
        BigDecimal total,
        BigDecimal optionalTotal,
        LocalDate validUntil,
        LocalDate estimatedStartDate,
        LocalDate estimatedEndDate,
        List<QuoteItemResponse> items
) {
}
