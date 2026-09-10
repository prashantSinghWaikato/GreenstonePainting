package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Entity
@Table(name = "admin_users")
public class AdminUser extends BaseEntity {

    @Version
    @Column(nullable = false)
    private long version;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdminRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private OffsetDateTime passwordChangedAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "assignment_notifications_enabled", nullable = false)
    private boolean assignmentNotificationsEnabled = true;

    @Column(name = "follow_up_notifications_enabled", nullable = false)
    private boolean followUpNotificationsEnabled = true;

    @Column(name = "daily_digest_enabled", nullable = false)
    private boolean dailyDigestEnabled = true;

    @Column(name = "job_notifications_enabled", nullable = false)
    private boolean jobNotificationsEnabled = true;

    protected AdminUser() {
    }

    public AdminUser(String email, String passwordHash, String displayName) {
        this(email, passwordHash, displayName, AdminRole.OWNER);
    }

    public AdminUser(String email, String passwordHash, String displayName, AdminRole role) {
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.displayName = displayName.trim();
        this.role = role;
        this.enabled = true;
        this.failedLoginAttempts = 0;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AdminRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getVersion() { return version; }
    public OffsetDateTime getLastLoginAt() { return lastLoginAt; }
    public OffsetDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public OffsetDateTime getLockedUntil() { return lockedUntil; }
    public boolean isAssignmentNotificationsEnabled() { return assignmentNotificationsEnabled; }
    public boolean isFollowUpNotificationsEnabled() { return followUpNotificationsEnabled; }
    public boolean isDailyDigestEnabled() { return dailyDigestEnabled; }
    public boolean isJobNotificationsEnabled() { return jobNotificationsEnabled; }

    public void setNotificationPreferences(boolean assignment, boolean followUp, boolean dailyDigest) {
        this.assignmentNotificationsEnabled = assignment;
        this.followUpNotificationsEnabled = followUp;
        this.dailyDigestEnabled = dailyDigest;
    }

    public void setNotificationPreferences(boolean assignment, boolean followUp, boolean dailyDigest, boolean jobs) {
        setNotificationPreferences(assignment, followUp, dailyDigest);
        this.jobNotificationsEnabled = jobs;
    }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public void recordSuccessfulLogin() {
        lastLoginAt = OffsetDateTime.now(ZoneOffset.UTC);
        failedLoginAttempts = 0;
        lockedUntil = null;
    }

    public boolean recordFailedLogin(int maximumAttempts, Duration lockDuration) {
        if (isLocked()) {
            return true;
        }
        failedLoginAttempts += 1;
        if (failedLoginAttempts >= maximumAttempts) {
            lockedUntil = OffsetDateTime.now(ZoneOffset.UTC).plus(lockDuration);
            failedLoginAttempts = 0;
            return true;
        }
        return false;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.passwordChangedAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            this.failedLoginAttempts = 0;
            this.lockedUntil = null;
        }
    }
}
