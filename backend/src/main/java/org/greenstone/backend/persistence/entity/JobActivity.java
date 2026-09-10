package org.greenstone.backend.persistence.entity;

import jakarta.persistence.*;

@Entity @Table(name = "job_activities")
public class JobActivity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false) private PaintingJob job;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "actor_admin_id") private AdminUser actor;
    @Enumerated(EnumType.STRING) @Column(name = "activity_type", nullable = false, length = 30) private JobActivityType type;
    @Column(nullable = false, length = 500) private String summary;
    @Column(name = "note_body", columnDefinition = "TEXT") private String noteBody;
    protected JobActivity() {}
    public JobActivity(PaintingJob job, AdminUser actor, JobActivityType type, String summary, String noteBody) { this.job = job; this.actor = actor; this.type = type; this.summary = summary; this.noteBody = noteBody; }
    public PaintingJob getJob() { return job; } public AdminUser getActor() { return actor; } public JobActivityType getType() { return type; }
    public String getSummary() { return summary; } public String getNoteBody() { return noteBody; }
}
