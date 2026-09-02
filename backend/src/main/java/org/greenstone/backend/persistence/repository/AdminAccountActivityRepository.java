package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.AdminAccountActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminAccountActivityRepository extends JpaRepository<AdminAccountActivity, UUID> {
    List<AdminAccountActivity> findTop50ByOrderByCreatedAtDesc();
}
