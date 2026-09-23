package org.greenstone.backend.admin.content;

import org.greenstone.backend.persistence.entity.ColourPaletteEntry;
import org.greenstone.backend.persistence.repository.ColourPaletteEntryRepository;
import org.greenstone.backend.web.ResourceNotFoundException;
import org.greenstone.backend.web.WorkflowConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
public class AdminColourService {

    private final ColourPaletteEntryRepository repository;

    public AdminColourService(ColourPaletteEntryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AdminColourResponse> list() {
        return repository.findAllByOrderByDisplayOrderAscNameAsc().stream().map(this::response).toList();
    }

    @Transactional
    public AdminColourResponse create(SaveAdminColourRequest request) {
        validateName(request.name(), null);
        var colour = new ColourPaletteEntry(clean(request.name()), normaliseHex(request.hex()), request.displayOrder());
        apply(colour, request);
        repository.saveAndFlush(colour);
        return response(colour);
    }

    @Transactional
    public AdminColourResponse update(UUID colourId, SaveAdminColourRequest request) {
        var colour = find(colourId);
        if (colour.getVersion() != request.version()) {
            throw new WorkflowConflictException("This colour changed in another session. Reload it before continuing.");
        }
        if (colour.isActive() && !request.active() && repository.countByActiveTrue() <= 1) {
            throw new WorkflowConflictException("Keep at least one colour visible on the website.");
        }
        validateName(request.name(), colourId);
        apply(colour, request);
        repository.flush();
        return response(colour);
    }

    @Transactional
    public void delete(UUID colourId) {
        var colour = find(colourId);
        if (colour.isActive() && repository.countByActiveTrue() <= 1) {
            throw new WorkflowConflictException("Keep at least one colour visible on the website.");
        }
        repository.delete(colour);
        repository.flush();
    }

    private void apply(ColourPaletteEntry colour, SaveAdminColourRequest request) {
        var reseneUrl = clean(request.reseneUrl());
        if (!reseneUrl.isEmpty()) validateUrl(reseneUrl);
        colour.setName(clean(request.name()));
        colour.setHex(normaliseHex(request.hex()));
        colour.setReseneUrl(reseneUrl.isEmpty() ? null : reseneUrl);
        colour.setActive(request.active());
        colour.setDisplayOrder(request.displayOrder());
        if (request.active() && request.defaultInterior()) clearInteriorDefault(colour.getId());
        if (request.active() && request.defaultExterior()) clearExteriorDefault(colour.getId());
        colour.setDefaultInterior(request.active() && request.defaultInterior());
        colour.setDefaultExterior(request.active() && request.defaultExterior());
    }

    private void clearInteriorDefault(UUID exceptId) {
        repository.findAll().stream()
                .filter(item -> item.isDefaultInterior() && !item.getId().equals(exceptId))
                .forEach(item -> item.setDefaultInterior(false));
    }

    private void clearExteriorDefault(UUID exceptId) {
        repository.findAll().stream()
                .filter(item -> item.isDefaultExterior() && !item.getId().equals(exceptId))
                .forEach(item -> item.setDefaultExterior(false));
    }

    private void validateName(String name, UUID colourId) {
        repository.findByNameIgnoreCase(clean(name)).ifPresent(existing -> {
            if (!existing.getId().equals(colourId)) throw new WorkflowConflictException("A colour with this name already exists.");
        });
    }

    private void validateUrl(String value) {
        try {
            var uri = URI.create(value);
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new WorkflowConflictException("Enter a complete Resene link beginning with https://.");
        }
    }

    private ColourPaletteEntry find(UUID colourId) {
        return repository.findById(colourId).orElseThrow(() -> new ResourceNotFoundException("Colour was not found."));
    }

    private AdminColourResponse response(ColourPaletteEntry colour) {
        return new AdminColourResponse(
                colour.getId(), colour.getName(), colour.getHex(), colour.getReseneUrl(), colour.isActive(),
                colour.getDisplayOrder(), colour.isDefaultInterior(), colour.isDefaultExterior(),
                colour.getVersion(), colour.getUpdatedAt()
        );
    }

    private String normaliseHex(String value) { return clean(value).toLowerCase(); }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
