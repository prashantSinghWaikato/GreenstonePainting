package org.greenstone.backend.enquiry;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateEnquiryRequest(
        @NotBlank(message = "Enter your first name.")
        @Size(max = 100, message = "First name must be 100 characters or fewer.")
        String firstName,

        @NotBlank(message = "Enter your last name.")
        @Size(max = 100, message = "Last name must be 100 characters or fewer.")
        String lastName,

        @NotBlank(message = "Enter your email address.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 254, message = "Email address must be 254 characters or fewer.")
        String email,

        @NotBlank(message = "Enter your phone number.")
        @Size(max = 40, message = "Phone number must be 40 characters or fewer.")
        @Pattern(
                regexp = "^(?=(?:\\D*\\d){7,15}\\D*$)[+\\d][\\d\\s().-]*$",
                message = "Enter a valid phone number using 7 to 15 digits."
        )
        String phone,

        @NotBlank(message = "Select the painting service you need.")
        @Size(max = 100, message = "Selected service is invalid.")
        String serviceSlug,

        @NotBlank(message = "Enter the property location.")
        @Size(max = 300, message = "Property location must be 300 characters or fewer.")
        String propertyAddress,

        @NotBlank(message = "Tell us about your painting project.")
        @Size(max = 10000, message = "Project description must be 10,000 characters or fewer.")
        String message
) {
}
