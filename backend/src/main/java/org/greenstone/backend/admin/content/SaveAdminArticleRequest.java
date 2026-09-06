package org.greenstone.backend.admin.content;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveAdminArticleRequest(
        @NotBlank(message = "Enter an article title.")
        @Size(max = 180, message = "Article title must be 180 characters or fewer.")
        String title,

        @NotBlank(message = "Enter a short title.")
        @Size(max = 120, message = "Short title must be 120 characters or fewer.")
        String shortTitle,

        @NotBlank(message = "Enter an article category.")
        @Size(max = 100, message = "Category must be 100 characters or fewer.")
        String topic,

        @NotBlank(message = "Enter an article summary.")
        @Size(max = 600, message = "Article summary must be 600 characters or fewer.")
        String excerpt,

        @NotBlank(message = "Enter the article content.")
        @Size(max = 20000, message = "Article content must be 20,000 characters or fewer.")
        String body,

        @Min(value = 1, message = "Read time must be at least one minute.")
        @Max(value = 60, message = "Read time must be 60 minutes or fewer.")
        int readTimeMinutes,

        long version
) {
}
