package org.greenstone.backend.security;

import org.greenstone.backend.persistence.entity.AdminRole;

public record AdminSessionResponse(String email, String displayName, AdminRole role) {
}
