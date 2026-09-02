package org.greenstone.backend.admin.content;

import jakarta.validation.constraints.NotNull;
import org.greenstone.backend.persistence.entity.PublicationStatus;

public record SetProjectPublicationRequest(
        @NotNull(message = "Choose a publication status.")
        PublicationStatus status,
        long version
) {
}
