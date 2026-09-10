package org.greenstone.backend.admin.job;

import jakarta.validation.constraints.*;
import org.greenstone.backend.persistence.entity.JobStatus;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateJobRequest(
        @NotNull JobStatus status,
        UUID assignedAdminId,
        LocalDate scheduledStartDate,
        LocalDate scheduledEndDate,
        @Size(max = 10000) String siteInstructions,
        @Size(max = 10000) String internalNotes,
        @NotNull @PositiveOrZero Long version
) {}
