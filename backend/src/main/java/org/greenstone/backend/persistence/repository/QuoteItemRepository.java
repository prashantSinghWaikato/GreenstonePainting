package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteItemRepository extends JpaRepository<QuoteItem, UUID> {
    List<QuoteItem> findAllByQuoteIdOrderByDisplayOrderAsc(UUID quoteId);
    void deleteAllByQuoteId(UUID quoteId);
}
