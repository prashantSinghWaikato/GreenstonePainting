package org.greenstone.backend.security;

import org.greenstone.backend.web.AccountSecurityException;

public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null || password.length() < 12 || password.length() > 100
                || password.chars().noneMatch(Character::isLetter)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new AccountSecurityException(
                    "Use 12 to 100 characters with at least one letter and one number."
            );
        }
    }
}
