package org.greenstone.backend.admin.staff;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SetAdminStaffEnabledRequest(
        @NotNull(message = "Choose an account status.")
        Boolean enabled,

        @NotNull(message = "The account version is required.")
        @PositiveOrZero(message = "The account version is invalid.")
        Long version
) {
}
