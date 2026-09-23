package org.greenstone.backend.persistence.repository;

import org.greenstone.backend.persistence.entity.ColourPaletteEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColourPaletteEntryRepository extends JpaRepository<ColourPaletteEntry, UUID> {
    List<ColourPaletteEntry> findAllByOrderByDisplayOrderAscNameAsc();
    List<ColourPaletteEntry> findAllByActiveTrueOrderByDisplayOrderAscNameAsc();
    Optional<ColourPaletteEntry> findByNameIgnoreCase(String name);
    long countByActiveTrue();
}
