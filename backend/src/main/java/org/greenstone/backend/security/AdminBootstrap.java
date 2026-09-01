package org.greenstone.backend.security;

import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String displayName;

    public AdminBootstrap(
            AdminUserRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.bootstrap.email:}") String email,
            @Value("${app.admin.bootstrap.password:}") String password,
            @Value("${app.admin.bootstrap.display-name:Greenstone Admin}") String displayName
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim();
        this.password = password;
        this.displayName = displayName.trim();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) {
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("ADMIN_EMAIL and ADMIN_PASSWORD must be supplied together.");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD must contain at least 12 characters.");
        }
        if (displayName.isBlank()) {
            throw new IllegalStateException("ADMIN_DISPLAY_NAME cannot be blank.");
        }

        repository.findByEmailIgnoreCase(email).orElseGet(() -> {
            var admin = new AdminUser(email, passwordEncoder.encode(password), displayName);
            log.info("Creating the initial admin account for {}", admin.getEmail());
            return repository.save(admin);
        });
    }
}
