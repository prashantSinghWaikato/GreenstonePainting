package org.greenstone.backend.admin.staff;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.greenstone.backend.persistence.entity.AdminRole;

public record CreateAdminStaffRequest(
        @NotBlank(message = "Enter the staff member's name.")
        @Size(max = 150, message = "Display name must be 150 characters or fewer.")
        String displayName,

        @NotBlank(message = "Enter the staff member's email address.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 254, message = "Email address is too long.")
        String email,

        @NotNull(message = "Choose an account role.")
        AdminRole role,

        @NotBlank(message = "Enter a temporary password.")
        @Size(min = 12, max = 100, message = "Temporary password must contain 12 to 100 characters.")
        String temporaryPassword
) {
}
