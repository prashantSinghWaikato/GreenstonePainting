package org.greenstone.backend.admin.notification;

import jakarta.validation.constraints.NotNull;

public record UpdateAdminNotificationPreferencesRequest(
        @NotNull Boolean assignmentNotificationsEnabled,
        @NotNull Boolean followUpNotificationsEnabled,
        @NotNull Boolean dailyDigestEnabled
) {
}
