package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;

@Entity
@Table(name = "blog_articles")
public class BlogArticle extends BaseEntity {

    @Version
    @Column(nullable = false)
    private long version;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(name = "short_title", nullable = false, length = 120)
    private String shortTitle;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(nullable = false, length = 600)
    private String excerpt;

    @Column(nullable = false, length = 20000)
    private String body;

    @Column(name = "read_time_minutes", nullable = false)
    private int readTimeMinutes;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    protected BlogArticle() {
    }

    public BlogArticle(String slug, String title, String shortTitle, String topic, String excerpt, String body, int readTimeMinutes) {
        this.slug = slug;
        this.title = title;
        this.shortTitle = shortTitle;
        this.topic = topic;
        this.excerpt = excerpt;
        this.body = body;
        this.readTimeMinutes = readTimeMinutes;
    }

    public long getVersion() { return version; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getShortTitle() { return shortTitle; }
    public void setShortTitle(String shortTitle) { this.shortTitle = shortTitle; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public int getReadTimeMinutes() { return readTimeMinutes; }
    public void setReadTimeMinutes(int readTimeMinutes) { this.readTimeMinutes = readTimeMinutes; }
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
