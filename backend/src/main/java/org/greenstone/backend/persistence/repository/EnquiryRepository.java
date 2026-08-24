package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID> {
    List<Enquiry> findAllByStatusOrderByCreatedAtDesc(EnquiryStatus status);
    long countByStatus(EnquiryStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select enquiry from Enquiry enquiry where enquiry.id = :id")
    Optional<Enquiry> findByIdForAttachmentUpload(@Param("id") UUID id);
}
