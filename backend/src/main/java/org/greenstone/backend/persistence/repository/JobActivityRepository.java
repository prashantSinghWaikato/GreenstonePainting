package org.greenstone.backend.persistence.repository;
import org.greenstone.backend.persistence.entity.JobActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface JobActivityRepository extends JpaRepository<JobActivity, UUID> { List<JobActivity> findAllByJobIdOrderByCreatedAtDesc(UUID jobId); }
