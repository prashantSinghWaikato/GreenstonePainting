package org.greenstone.backend.web;

public class AdminAccountLockedException extends RuntimeException {
    public AdminAccountLockedException() {
        super("Too many unsuccessful sign-in attempts. Try again in 15 minutes.");
    }
}
