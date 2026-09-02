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
@Table(name = "project_activities")
public class ProjectActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private PortfolioProject project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_admin_id", nullable = false)
    private AdminUser actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private ProjectActivityType activityType;

    @Column(nullable = false, length = 500)
    private String summary;

    protected ProjectActivity() {
    }

    public ProjectActivity(PortfolioProject project, AdminUser actor, ProjectActivityType activityType, String summary) {
        this.project = project;
        this.actor = actor;
        this.activityType = activityType;
        this.summary = summary;
    }

    public ProjectActivityType getActivityType() { return activityType; }
    public String getSummary() { return summary; }
    public AdminUser getActor() { return actor; }
}
