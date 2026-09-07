package org.greenstone.backend.publiccontent;

import java.util.List;

public record PublicServiceResponse(
        String slug,
        String title,
        String label,
        String summary,
        String description,
        List<String> inclusions,
        String note,
        int displayOrder,
        String imageUrl,
        String imageAlt
) {}
