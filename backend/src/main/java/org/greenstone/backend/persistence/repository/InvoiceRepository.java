package org.greenstone.backend.persistence.repository;
import org.greenstone.backend.persistence.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> { Optional<Invoice> findByJobId(UUID jobId); List<Invoice> findAllByOrderByCreatedAtDesc(); }
