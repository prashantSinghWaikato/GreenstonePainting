package org.greenstone.backend.publiccontent;

import java.util.UUID;

public record PublicColourResponse(
        UUID id,
        String name,
        String hex,
        String reseneUrl,
        int displayOrder,
        boolean defaultInterior,
        boolean defaultExterior
) {}
