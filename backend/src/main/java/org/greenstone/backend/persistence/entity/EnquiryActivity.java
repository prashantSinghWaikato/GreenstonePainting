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
@Table(name = "enquiry_activities")
public class EnquiryActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private Enquiry enquiry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_admin_id", nullable = false)
    private AdminUser actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private EnquiryActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private EnquiryStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 30)
    private EnquiryStatus newStatus;

    @Column(nullable = false, length = 500)
    private String summary;

    protected EnquiryActivity() {
    }

    public EnquiryActivity(
            Enquiry enquiry,
            AdminUser actor,
            EnquiryActivityType activityType,
            EnquiryStatus previousStatus,
            EnquiryStatus newStatus,
            String summary
    ) {
        this.enquiry = enquiry;
        this.actor = actor;
        this.activityType = activityType;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.summary = summary;
    }

    public EnquiryActivityType getActivityType() { return activityType; }
    public EnquiryStatus getPreviousStatus() { return previousStatus; }
    public EnquiryStatus getNewStatus() { return newStatus; }
    public String getSummary() { return summary; }
    public AdminUser getActor() { return actor; }
}
