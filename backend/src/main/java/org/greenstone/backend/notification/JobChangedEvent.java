package org.greenstone.backend.notification;
import org.greenstone.backend.persistence.entity.NotificationType;
import java.util.UUID;
public record JobChangedEvent(UUID jobId, UUID recipientId, NotificationType type, String key) {}
