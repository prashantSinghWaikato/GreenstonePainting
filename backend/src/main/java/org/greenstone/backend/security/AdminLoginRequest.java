package org.greenstone.backend.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminLoginRequest(
        @NotBlank(message = "Enter your staff email address.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 254, message = "Email address is too long.")
        String email,

        @NotBlank(message = "Enter your password.")
        @Size(max = 200, message = "Password is too long.")
        String password
) {
}
