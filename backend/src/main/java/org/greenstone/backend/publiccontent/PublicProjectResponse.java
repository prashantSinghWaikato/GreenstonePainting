package org.greenstone.backend.publiccontent;

import java.util.List;

public record PublicProjectResponse(
        String slug,
        String title,
        String category,
        String location,
        String summary,
        List<String> highlights,
        String imageUrl,
        String imageAlt
) {
}
