package org.greenstone.backend.admin.job;

import org.greenstone.backend.persistence.entity.JobStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

public record AdminJobSummaryResponse(
        UUID id, String jobNumber, JobStatus status, String customerName, String title, String serviceTitle,
        String propertyAddress, UUID assignedAdminId, String assignedDisplayName, LocalDate scheduledStartDate,
        LocalDate scheduledEndDate, long photoCount, UUID quoteId, String quoteNumber, BigDecimal quoteTotal,
        OffsetDateTime updatedAt
) {}
