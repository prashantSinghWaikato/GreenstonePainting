package org.greenstone.backend.admin.job;
import java.time.OffsetDateTime;
import java.util.UUID;
public record AdminJobChecklistResponse(UUID id, String label, int position, boolean completed, OffsetDateTime completedAt, String completedBy) {}
