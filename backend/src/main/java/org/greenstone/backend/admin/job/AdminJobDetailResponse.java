package org.greenstone.backend.admin.job;

import org.greenstone.backend.persistence.entity.JobStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record AdminJobDetailResponse(
        UUID id, UUID quoteId, UUID enquiryId, String jobNumber, long version, JobStatus status,
        String customerName, String customerEmail, String customerPhone, String propertyAddress,
        String title, String serviceTitle, String scope, String siteInstructions, String internalNotes,
        UUID assignedAdminId, String assignedDisplayName, LocalDate scheduledStartDate, LocalDate scheduledEndDate,
        OffsetDateTime actualStartedAt, OffsetDateTime completedAt, String customerSignoffName, OffsetDateTime customerSignoffAt,
        String quoteNumber, BigDecimal quoteTotal, AdminJobInvoiceSummary invoice,
        OffsetDateTime createdAt, OffsetDateTime updatedAt, List<AdminJobPhotoResponse> photos,
        List<AdminJobChecklistResponse> checklist, List<AdminJobActivityResponse> activities
) {}
