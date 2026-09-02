package org.greenstone.backend.admin.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeAdminPasswordRequest(
        @NotBlank(message = "Enter your current password.")
        @Size(max = 200, message = "Current password is too long.")
        String currentPassword,

        @NotBlank(message = "Enter a new password.")
        @Size(min = 12, max = 100, message = "New password must contain 12 to 100 characters.")
        String newPassword
) {
}
