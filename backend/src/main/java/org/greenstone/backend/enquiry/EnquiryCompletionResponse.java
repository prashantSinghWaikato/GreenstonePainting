package org.greenstone.backend.enquiry;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EnquiryCompletionResponse(
        UUID id,
        OffsetDateTime completedAt,
        boolean notificationSent
) {
}
