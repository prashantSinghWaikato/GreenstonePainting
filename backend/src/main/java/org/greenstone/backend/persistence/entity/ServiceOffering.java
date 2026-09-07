package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;

@Entity
@Table(name = "service_offerings")
public class ServiceOffering extends BaseEntity {

    @Version
    @Column(nullable = false)
    private long version;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, length = 10000)
    private String description;

    @Column(nullable = false, length = 100)
    private String label = "";

    @Column(nullable = false, length = 3000)
    private String inclusions = "";

    @Column(name = "service_note", nullable = false, length = 500)
    private String note = "";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "featured_image_object_key", length = 500)
    private String featuredImageObjectKey;

    @Column(name = "featured_image_alt", length = 250)
    private String featuredImageAlt;

    @Column(name = "featured_image_filename", length = 255)
    private String featuredImageFilename;

    @Column(name = "featured_image_content_type", length = 150)
    private String featuredImageContentType;

    @Column(name = "featured_image_size_bytes")
    private Long featuredImageSizeBytes;

    protected ServiceOffering() {
    }

    public ServiceOffering(String slug, String title, String summary, String description, int displayOrder) {
        this.slug = slug;
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public String getSlug() { return slug; }
    public long getVersion() { return version; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getInclusions() { return inclusions; }
    public void setInclusions(String inclusions) { this.inclusions = inclusions; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(OffsetDateTime publishedAt) { this.publishedAt = publishedAt; }
    public String getFeaturedImageObjectKey() { return featuredImageObjectKey; }
    public String getFeaturedImageAlt() { return featuredImageAlt; }
    public String getFeaturedImageFilename() { return featuredImageFilename; }
    public String getFeaturedImageContentType() { return featuredImageContentType; }
    public Long getFeaturedImageSizeBytes() { return featuredImageSizeBytes; }

    public void setFeaturedImage(String objectKey, String alt, String filename, String contentType, long sizeBytes) {
        featuredImageObjectKey = objectKey;
        featuredImageAlt = alt;
        featuredImageFilename = filename;
        featuredImageContentType = contentType;
        featuredImageSizeBytes = sizeBytes;
    }

    public void clearFeaturedImage() {
        featuredImageObjectKey = null;
        featuredImageAlt = null;
        featuredImageFilename = null;
        featuredImageContentType = null;
        featuredImageSizeBytes = null;
    }
}
