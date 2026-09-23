package org.greenstone.backend.admin.content;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveAdminColourRequest(
        @NotBlank(message = "Enter a colour name.")
        @Size(max = 100, message = "Colour name must be 100 characters or fewer.")
        String name,

        @NotBlank(message = "Enter a hexadecimal colour value.")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Use a six-digit hexadecimal value such as #e9e7e3.")
        String hex,

        @Size(max = 1000, message = "Resene link must be 1,000 characters or fewer.")
        String reseneUrl,

        boolean active,

        @Min(value = 1, message = "Display order must be at least 1.")
        @Max(value = 1000, message = "Display order must be 1,000 or fewer.")
        int displayOrder,

        boolean defaultInterior,
        boolean defaultExterior,
        long version
) {}
