package org.greenstone.backend.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "job_checklist_items")
public class JobChecklistItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false) private PaintingJob job;
    @Column(nullable = false, length = 300) private String label;
    @Column(nullable = false) private int position;
    @Column(nullable = false) private boolean completed;
    @Column(name = "completed_at") private OffsetDateTime completedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "completed_by_admin_id") private AdminUser completedBy;
    protected JobChecklistItem() {}
    public JobChecklistItem(PaintingJob job, String label, int position) { this.job=job; this.label=label; this.position=position; }
    public PaintingJob getJob(){return job;} public String getLabel(){return label;} public int getPosition(){return position;} public boolean isCompleted(){return completed;} public OffsetDateTime getCompletedAt(){return completedAt;} public AdminUser getCompletedBy(){return completedBy;}
    public void setCompleted(boolean value, AdminUser actor, OffsetDateTime now){completed=value; completedBy=value?actor:null; completedAt=value?now:null;}
}
