package org.greenstone.backend.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.scheduling.annotation.Async;

@Component
@ConditionalOnProperty(name = "app.notification.automation-enabled", havingValue = "true", matchIfMissing = true)
public class WorkflowNotificationAutomation {

    private final WorkflowNotificationService service;

    public WorkflowNotificationAutomation(WorkflowNotificationService service) {
        this.service = service;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void assignmentChanged(EnquiryAssignedEvent event) {
        service.deliverAssignment(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void jobChanged(JobChangedEvent event) { service.deliverJobEvent(event); }

    @Scheduled(cron = "${app.notification.follow-up-cron:0 * * * * *}", zone = "Pacific/Auckland")
    public void followUps() {
        service.deliverDueFollowUps();
        service.deliverJobReminders();
        service.retryFailures();
    }

    @Scheduled(cron = "${app.notification.daily-digest-cron:0 0 8 * * MON-FRI}", zone = "Pacific/Auckland")
    public void dailyDigest() {
        service.deliverDailyDigests();
    }
}
