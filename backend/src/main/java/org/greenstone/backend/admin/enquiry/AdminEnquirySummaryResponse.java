package org.greenstone.backend.admin.enquiry;

import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.entity.EnquiryPriority;

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
        UUID assignedAdminId,
        String assignedDisplayName,
        EnquiryPriority priority,
        OffsetDateTime followUpAt,
        boolean overdue,
        long attachmentCount,
        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {
}
