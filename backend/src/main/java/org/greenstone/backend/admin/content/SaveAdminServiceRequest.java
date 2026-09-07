package org.greenstone.backend.admin.content;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveAdminServiceRequest(
        @NotBlank(message = "Enter a service title.")
        @Size(max = 150, message = "Service title must be 150 characters or fewer.")
        String title,

        @NotBlank(message = "Enter a service label.")
        @Size(max = 100, message = "Service label must be 100 characters or fewer.")
        String label,

        @NotBlank(message = "Enter a short service summary.")
        @Size(max = 500, message = "Service summary must be 500 characters or fewer.")
        String summary,

        @NotBlank(message = "Enter the detailed service description.")
        @Size(max = 10000, message = "Service description must be 10,000 characters or fewer.")
        String description,

        @NotNull(message = "Add at least one service inclusion.")
        @Size(min = 1, max = 12, message = "Add between 1 and 12 service inclusions.")
        List<@NotBlank(message = "Service inclusions cannot be empty.") @Size(max = 180, message = "Each inclusion must be 180 characters or fewer.") String> inclusions,

        @NotBlank(message = "Enter a service planning note.")
        @Size(max = 500, message = "Service note must be 500 characters or fewer.")
        String note,

        @Min(value = 1, message = "Display order must be at least 1.")
        @Max(value = 100, message = "Display order must be 100 or fewer.")
        int displayOrder,

        long version
) {}
