package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.ProjectActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, UUID> {
    List<ProjectActivity> findTop30ByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
