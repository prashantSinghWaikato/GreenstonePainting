package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_account_activities")
public class AdminAccountActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_admin_id", nullable = false)
    private AdminUser actor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_admin_id", nullable = false)
    private AdminUser target;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private AdminAccountActivityType activityType;

    @Column(nullable = false, length = 500)
    private String summary;

    protected AdminAccountActivity() {
    }

    public AdminAccountActivity(
            AdminUser actor,
            AdminUser target,
            AdminAccountActivityType activityType,
            String summary
    ) {
        this.actor = actor;
        this.target = target;
        this.activityType = activityType;
        this.summary = summary;
    }

    public AdminUser getActor() { return actor; }
    public AdminUser getTarget() { return target; }
    public AdminAccountActivityType getActivityType() { return activityType; }
    public String getSummary() { return summary; }
}
