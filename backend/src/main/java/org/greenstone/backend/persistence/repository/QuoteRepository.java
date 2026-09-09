package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    List<Quote> findAllByEnquiryIdOrderByRevisionNumberDesc(UUID enquiryId);
    Optional<Quote> findFirstByEnquiryIdOrderByRevisionNumberDesc(UUID enquiryId);
    Optional<Quote> findByResponseTokenHash(String responseTokenHash);
}
