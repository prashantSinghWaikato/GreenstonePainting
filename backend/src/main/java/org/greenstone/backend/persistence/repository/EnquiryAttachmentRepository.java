package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.EnquiryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnquiryAttachmentRepository extends JpaRepository<EnquiryAttachment, UUID> {
    List<EnquiryAttachment> findAllByEnquiryId(UUID enquiryId);
    long countByEnquiryId(UUID enquiryId);
    Optional<EnquiryAttachment> findByIdAndEnquiryId(UUID id, UUID enquiryId);

    @Query("""
            select attachment.enquiry.id as enquiryId, count(attachment) as total
            from EnquiryAttachment attachment
            where attachment.enquiry.id in :enquiryIds
            group by attachment.enquiry.id
            """)
    List<EnquiryAttachmentCount> countAllByEnquiryIds(@Param("enquiryIds") List<UUID> enquiryIds);

    interface EnquiryAttachmentCount {
        UUID getEnquiryId();
        long getTotal();
    }
}
