package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, UUID> {
    List<ProjectImage> findAllByProjectIdOrderByDisplayOrderAsc(UUID projectId);
}
