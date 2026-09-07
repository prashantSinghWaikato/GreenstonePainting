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
@Table(name = "service_offering_activities")
public class ServiceOfferingActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffering service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_admin_id", nullable = false)
    private AdminUser actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private ServiceOfferingActivityType activityType;

    @Column(nullable = false, length = 500)
    private String summary;

    protected ServiceOfferingActivity() {}

    public ServiceOfferingActivity(ServiceOffering service, AdminUser actor, ServiceOfferingActivityType activityType, String summary) {
        this.service = service;
        this.actor = actor;
        this.activityType = activityType;
        this.summary = summary;
    }

    public ServiceOfferingActivityType getActivityType() { return activityType; }
    public AdminUser getActor() { return actor; }
    public String getSummary() { return summary; }
}
