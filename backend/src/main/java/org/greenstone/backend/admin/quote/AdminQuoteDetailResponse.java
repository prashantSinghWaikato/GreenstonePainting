package org.greenstone.backend.admin.quote;

import org.greenstone.backend.persistence.entity.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminQuoteDetailResponse(
        UUID id,
        UUID enquiryId,
        String quoteNumber,
        int revisionNumber,
        long version,
        QuoteStatus status,
        String customerName,
        String customerEmail,
        String propertyAddress,
        String title,
        String scope,
        String terms,
        BigDecimal gstRate,
        BigDecimal subtotal,
        BigDecimal gstAmount,
        BigDecimal total,
        BigDecimal optionalTotal,
        LocalDate validUntil,
        LocalDate estimatedStartDate,
        LocalDate estimatedEndDate,
        OffsetDateTime sentAt,
        OffsetDateTime acceptedAt,
        OffsetDateTime declinedAt,
        String declineReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<QuoteItemResponse> items,
        List<QuoteActivityResponse> activities
) {
}
