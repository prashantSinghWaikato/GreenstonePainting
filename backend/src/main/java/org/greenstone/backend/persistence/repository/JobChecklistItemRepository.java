package org.greenstone.backend.persistence.repository;
import org.greenstone.backend.persistence.entity.JobChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface JobChecklistItemRepository extends JpaRepository<JobChecklistItem, UUID> { List<JobChecklistItem> findAllByJobIdOrderByPositionAsc(UUID jobId); Optional<JobChecklistItem> findByIdAndJobId(UUID id, UUID jobId); }
