package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.PublicationStatus;
import org.greenstone.backend.persistence.entity.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TestimonialRepository extends JpaRepository<Testimonial, UUID> {
    List<Testimonial> findAllByStatusOrderByCreatedAtDesc(PublicationStatus status);
    List<Testimonial> findAllByFeaturedTrueAndStatusOrderByCreatedAtDesc(PublicationStatus status);
}
