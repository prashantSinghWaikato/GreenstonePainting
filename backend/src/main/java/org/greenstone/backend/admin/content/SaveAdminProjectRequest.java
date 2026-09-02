package org.greenstone.backend.admin.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SaveAdminProjectRequest(
        @NotBlank(message = "Enter a project title.")
        @Size(max = 180, message = "Project title must be 180 characters or fewer.")
        String title,

        @NotBlank(message = "Enter a short project summary.")
        @Size(max = 600, message = "Project summary must be 600 characters or fewer.")
        String summary,

        @NotBlank(message = "Enter the project details or highlights.")
        @Size(max = 10000, message = "Project details must be 10,000 characters or fewer.")
        String description,

        @NotBlank(message = "Enter the project location.")
        @Size(max = 150, message = "Project location must be 150 characters or fewer.")
        String location,

        LocalDate completedOn,

        @Size(max = 100, message = "Service selection is invalid.")
        String serviceSlug,

        boolean featured,

        long version
) {
}
