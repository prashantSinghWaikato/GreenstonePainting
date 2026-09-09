package org.greenstone.backend.admin.enquiry;

import org.greenstone.backend.persistence.entity.ContactPreference;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.entity.EnquiryType;
import org.greenstone.backend.persistence.entity.EnquiryPriority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminEnquiryDetailResponse(
        UUID id,
        String reference,
        long version,
        EnquiryType type,
        EnquiryStatus status,
        UUID assignedAdminId,
        String assignedDisplayName,
        EnquiryPriority priority,
        OffsetDateTime followUpAt,
        boolean overdue,
        String firstName,
        String lastName,
        String email,
        String phone,
        ContactPreference contactPreference,
        String serviceSlug,
        String serviceTitle,
        String propertyAddress,
        String suburb,
        String message,
        BigDecimal estimatedBudget,
        LocalDate desiredStartDate,
        String internalNotes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime completedAt,
        OffsetDateTime notificationSentAt,
        List<AdminEnquiryAttachmentResponse> attachments,
        List<AdminEnquiryActivityResponse> activities
) {
}
