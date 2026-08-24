package org.greenstone.backend.web;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String message,
        Map<String, String> fieldErrors
) {
}
