package org.greenstone.backend.persistence.entity;

import jakarta.persistence.*;

@Entity @Table(name = "job_photos")
public class JobPhoto extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false) private PaintingJob job;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private JobPhotoPhase phase;
    @Column(name = "object_key", nullable = false, length = 500) private String objectKey;
    @Column(name = "original_filename", nullable = false, length = 255) private String originalFilename;
    @Column(name = "content_type", nullable = false, length = 150) private String contentType;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    protected JobPhoto() {}
    public JobPhoto(PaintingJob job, JobPhotoPhase phase, String key, String name, String type, long size) { this.job = job; this.phase = phase; objectKey = key; originalFilename = name; contentType = type; sizeBytes = size; }
    public PaintingJob getJob() { return job; } public JobPhotoPhase getPhase() { return phase; } public String getObjectKey() { return objectKey; }
    public String getOriginalFilename() { return originalFilename; } public String getContentType() { return contentType; } public long getSizeBytes() { return sizeBytes; }
}
