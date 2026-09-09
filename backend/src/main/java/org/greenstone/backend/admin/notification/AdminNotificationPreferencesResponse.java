package org.greenstone.backend.admin.notification;

public record AdminNotificationPreferencesResponse(
        boolean assignmentNotificationsEnabled,
        boolean followUpNotificationsEnabled,
        boolean dailyDigestEnabled
) {
}
