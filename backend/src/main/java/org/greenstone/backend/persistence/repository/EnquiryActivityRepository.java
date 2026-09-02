package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.EnquiryActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnquiryActivityRepository extends JpaRepository<EnquiryActivity, UUID> {
    List<EnquiryActivity> findAllByEnquiryIdOrderByCreatedAtDesc(UUID enquiryId);
}
