package org.greenstone.backend.admin.enquiry;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminEnquiryAttachmentResponse(
        UUID id,
        String filename,
        String contentType,
        long sizeBytes,
        OffsetDateTime createdAt
) {
}
