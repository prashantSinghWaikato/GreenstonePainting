package org.greenstone.backend.admin.enquiry;

import org.greenstone.backend.persistence.entity.EnquiryActivityType;
import org.greenstone.backend.persistence.entity.EnquiryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminEnquiryActivityResponse(
        UUID id,
        EnquiryActivityType type,
        EnquiryStatus previousStatus,
        EnquiryStatus newStatus,
        String summary,
        String noteBody,
        String actorDisplayName,
        OffsetDateTime createdAt
) {
}
