package org.greenstone.backend.admin.enquiry;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.greenstone.backend.persistence.entity.EnquiryStatus;

public record UpdateEnquiryWorkflowRequest(
        @NotNull(message = "Choose an enquiry status.")
        EnquiryStatus status,

        @Size(max = 10000, message = "Internal notes must be 10,000 characters or fewer.")
        String internalNotes,

        @NotNull(message = "The enquiry version is required.")
        @PositiveOrZero(message = "The enquiry version is invalid.")
        Long version
) {
}
