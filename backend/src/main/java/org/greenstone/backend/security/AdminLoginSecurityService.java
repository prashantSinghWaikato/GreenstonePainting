package org.greenstone.backend.security;

import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class AdminLoginSecurityService {

    private static final int MAXIMUM_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final AdminUserRepository repository;

    public AdminLoginSecurityService(AdminUserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean recordFailedLogin(String email) {
        return repository.findByEmailIgnoreCase(email)
                .filter(admin -> admin.isEnabled())
                .map(admin -> admin.recordFailedLogin(MAXIMUM_ATTEMPTS, LOCK_DURATION))
                .orElse(false);
    }

    @Transactional
    public void recordSuccessfulLogin(String email) {
        repository.findByEmailIgnoreCase(email).ifPresent(admin -> admin.recordSuccessfulLogin());
    }
}
