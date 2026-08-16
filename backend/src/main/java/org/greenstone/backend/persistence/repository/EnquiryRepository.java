package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {
    List<Enquiry> findAllByStatusOrderByCreatedAtDesc(EnquiryStatus status);
    long countByStatus(EnquiryStatus status);
}
