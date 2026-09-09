package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.QuoteActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteActivityRepository extends JpaRepository<QuoteActivity, UUID> {
    List<QuoteActivity> findAllByQuoteIdOrderByCreatedAtDesc(UUID quoteId);
}
