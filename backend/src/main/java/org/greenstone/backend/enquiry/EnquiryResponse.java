package org.greenstone.backend.enquiry;

import org.greenstone.backend.persistence.entity.EnquiryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EnquiryResponse(
        UUID id,
        EnquiryStatus status,
        OffsetDateTime createdAt,
        String uploadToken,
        OffsetDateTime uploadExpiresAt
) {
}
