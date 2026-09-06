package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.BlogArticleActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlogArticleActivityRepository extends JpaRepository<BlogArticleActivity, UUID> {
    List<BlogArticleActivity> findTop30ByArticleIdOrderByCreatedAtDesc(UUID articleId);
}
