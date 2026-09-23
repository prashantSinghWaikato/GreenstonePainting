package org.greenstone.backend.admin.content;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminColourResponse(
        UUID id,
        String name,
        String hex,
        String reseneUrl,
        boolean active,
        int displayOrder,
        boolean defaultInterior,
        boolean defaultExterior,
        long version,
        OffsetDateTime updatedAt
) {}
