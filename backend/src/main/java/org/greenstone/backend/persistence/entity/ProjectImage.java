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
@Table(name = "project_images")
public class ProjectImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private PortfolioProject project;

    @Column(name = "object_key", nullable = false, length = 500)
    private String objectKey;

    @Column(name = "alt_text", nullable = false, length = 250)
    private String altText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectImagePhase phase = ProjectImagePhase.GALLERY;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected ProjectImage() {
    }

    public ProjectImage(PortfolioProject project, String objectKey, String altText) {
        this.project = project;
        this.objectKey = objectKey;
        this.altText = altText;
    }

    public PortfolioProject getProject() { return project; }
    public void setProject(PortfolioProject project) { this.project = project; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    public ProjectImagePhase getPhase() { return phase; }
    public void setPhase(ProjectImagePhase phase) { this.phase = phase; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
