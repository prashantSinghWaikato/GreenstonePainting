package org.greenstone.backend.admin.staff;

import org.greenstone.backend.persistence.entity.AdminRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminStaffResponse(
        UUID id,
        String email,
        String displayName,
        AdminRole role,
        boolean enabled,
        OffsetDateTime lastLoginAt,
        OffsetDateTime passwordChangedAt,
        OffsetDateTime lockedUntil,
        OffsetDateTime createdAt,
        long version
) {
}
