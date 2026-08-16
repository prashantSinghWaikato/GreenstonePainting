package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.PortfolioProject;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioProjectRepository extends JpaRepository<PortfolioProject, UUID> {
    Optional<PortfolioProject> findBySlug(String slug);
    List<PortfolioProject> findAllByStatusOrderByCompletedOnDesc(PublicationStatus status);
    List<PortfolioProject> findAllByFeaturedTrueAndStatusOrderByCompletedOnDesc(PublicationStatus status);
}
