package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.NotificationDelivery;
import org.greenstone.backend.persistence.entity.NotificationDeliveryStatus;
import org.greenstone.backend.persistence.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
    Optional<NotificationDelivery> findByRecipientIdAndTypeAndDeduplicationKey(
            UUID recipientId,
            NotificationType type,
            String deduplicationKey
    );

    List<NotificationDelivery> findAllByStatusAndAttemptCountLessThanAndLastAttemptAtBefore(
            NotificationDeliveryStatus status,
            int maximumAttempts,
            OffsetDateTime retryBefore
    );
}
