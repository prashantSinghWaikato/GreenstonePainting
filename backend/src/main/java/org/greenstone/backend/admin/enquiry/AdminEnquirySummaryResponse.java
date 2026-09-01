package org.greenstone.backend.admin.enquiry;

import org.greenstone.backend.persistence.entity.EnquiryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminEnquirySummaryResponse(
        UUID id,
        String reference,
        String firstName,
        String lastName,
        String email,
        String phone,
        String serviceSlug,
        String serviceTitle,
        String propertyAddress,
        EnquiryStatus status,
        long attachmentCount,
        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {
}
