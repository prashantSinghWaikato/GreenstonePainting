package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, UUID> {
    Optional<ServiceOffering> findBySlug(String slug);
    List<ServiceOffering> findAllByActiveTrueOrderByDisplayOrderAsc();
}
