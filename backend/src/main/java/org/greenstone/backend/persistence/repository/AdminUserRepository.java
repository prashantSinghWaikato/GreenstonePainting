package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.AdminUser;
import org.greenstone.backend.persistence.entity.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {

    Optional<AdminUser> findByEmailIgnoreCase(String email);
    List<AdminUser> findAllByOrderByDisplayNameAsc();
    long countByRoleAndEnabledTrue(AdminRole role);
}
