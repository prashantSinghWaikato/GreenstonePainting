package org.greenstone.backend.admin.job;

import org.greenstone.backend.persistence.entity.JobPhotoPhase;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminJobPhotoResponse(UUID id, JobPhotoPhase phase, String filename, String contentType, long sizeBytes, OffsetDateTime createdAt) {}
