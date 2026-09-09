package org.greenstone.backend.admin.enquiry;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.greenstone.backend.persistence.entity.EnquiryPriority;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateEnquiryWorkflowRequest(
        @NotNull(message = "Choose an enquiry status.")
        EnquiryStatus status,

        UUID assignedAdminId,

        @NotNull(message = "Choose an enquiry priority.")
        EnquiryPriority priority,

        OffsetDateTime followUpAt,

        @Size(max = 3000, message = "A private note must be 3,000 characters or fewer.")
        String newNote,

        @NotNull(message = "The enquiry version is required.")
        @PositiveOrZero(message = "The enquiry version is invalid.")
        Long version
) {
}
