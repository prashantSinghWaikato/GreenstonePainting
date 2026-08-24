package org.greenstone.backend.enquiry;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EnquiryAttachmentResponse(
        UUID id,
        String originalFilename,
        String contentType,
        long sizeBytes,
        OffsetDateTime createdAt
) {
}
