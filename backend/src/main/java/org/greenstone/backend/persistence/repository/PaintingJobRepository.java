package org.greenstone.backend.persistence.repository;
import org.greenstone.backend.persistence.entity.PaintingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PaintingJobRepository extends JpaRepository<PaintingJob, UUID> { Optional<PaintingJob> findByQuoteId(UUID quoteId); List<PaintingJob> findAllByOrderByCreatedAtDesc(); }
