package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.Enquiry;
import org.greenstone.backend.persistence.entity.EnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;

public interface EnquiryRepository extends JpaRepository<Enquiry, UUID>, JpaSpecificationExecutor<Enquiry> {
    List<Enquiry> findAllByStatusOrderByCreatedAtDesc(EnquiryStatus status);
    long countByStatus(EnquiryStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select enquiry from Enquiry enquiry where enquiry.id = :id")
    Optional<Enquiry> findByIdForAttachmentUpload(@Param("id") UUID id);

    @Query("select enquiry.status as status, count(enquiry) as total from Enquiry enquiry group by enquiry.status")
    List<EnquiryStatusCount> countAllByStatus();

    @Query("""
            select enquiry from Enquiry enquiry
            join fetch enquiry.assignedTo assigned
            where enquiry.followUpAt is not null
              and enquiry.followUpAt <= :now
              and enquiry.status in ('NEW', 'IN_REVIEW', 'CONTACTED', 'QUOTED')
              and assigned.enabled = true
              and assigned.followUpNotificationsEnabled = true
            """)
    List<Enquiry> findAllDueForNotification(@Param("now") OffsetDateTime now);

    @Query("""
            select enquiry from Enquiry enquiry
            where enquiry.assignedTo.id = :adminId
              and enquiry.followUpAt is not null
              and enquiry.followUpAt < :now
              and enquiry.status in ('NEW', 'IN_REVIEW', 'CONTACTED', 'QUOTED')
            order by enquiry.followUpAt asc
            """)
    List<Enquiry> findAllOverdueForAdmin(@Param("adminId") UUID adminId, @Param("now") OffsetDateTime now);

    @Query("""
            select count(enquiry) from Enquiry enquiry
            where enquiry.assignedTo is null
              and enquiry.status in ('NEW', 'IN_REVIEW', 'CONTACTED', 'QUOTED')
            """)
    long countActiveUnassigned();

    interface EnquiryStatusCount {
        EnquiryStatus getStatus();
        long getTotal();
    }
}
