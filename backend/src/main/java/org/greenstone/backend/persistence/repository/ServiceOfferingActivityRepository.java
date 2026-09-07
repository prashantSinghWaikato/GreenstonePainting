package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.ServiceOfferingActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceOfferingActivityRepository extends JpaRepository<ServiceOfferingActivity, UUID> {
    List<ServiceOfferingActivity> findTop30ByServiceIdOrderByCreatedAtDesc(UUID serviceId);
}
