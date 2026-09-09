package org.greenstone.backend.notification;

import java.util.UUID;

public record EnquiryAssignedEvent(UUID enquiryId, UUID recipientId, long enquiryVersion) {
}
