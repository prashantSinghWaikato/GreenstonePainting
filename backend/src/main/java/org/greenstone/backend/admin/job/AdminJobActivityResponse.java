package org.greenstone.backend.admin.job;

import org.greenstone.backend.persistence.entity.JobActivityType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminJobActivityResponse(UUID id, JobActivityType type, String summary, String noteBody, String actorDisplayName, OffsetDateTime createdAt) {}
