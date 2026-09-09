package org.greenstone.backend.admin.enquiry;

import java.util.UUID;

public record AdminStaffOptionResponse(
        UUID id,
        String displayName,
        String email,
        boolean enabled
) {
}
