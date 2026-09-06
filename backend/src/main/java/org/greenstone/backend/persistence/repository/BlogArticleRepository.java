package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.BlogArticle;
import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogArticleRepository extends JpaRepository<BlogArticle, UUID> {
    Optional<BlogArticle> findBySlug(String slug);
    Optional<BlogArticle> findBySlugAndStatus(String slug, PublicationStatus status);
    List<BlogArticle> findAllByOrderByUpdatedAtDesc();
    List<BlogArticle> findAllByStatusOrderByPublishedAtDesc(PublicationStatus status);
}
