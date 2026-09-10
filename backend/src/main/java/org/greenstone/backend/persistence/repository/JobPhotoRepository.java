package org.greenstone.backend.persistence.repository;
import org.greenstone.backend.persistence.entity.JobPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface JobPhotoRepository extends JpaRepository<JobPhoto, UUID> { List<JobPhoto> findAllByJobIdOrderByCreatedAtDesc(UUID jobId); Optional<JobPhoto> findByIdAndJobId(UUID id, UUID jobId); long countByJobId(UUID jobId); }
