package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnquiryAttachmentRepository extends JpaRepository<EnquiryAttachment, UUID> {
    List<EnquiryAttachment> findAllByEnquiryId(UUID enquiryId);
}
