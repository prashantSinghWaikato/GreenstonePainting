package org.greenstone.backend.publiccontent;

import org.greenstone.backend.persistence.repository.ColourPaletteEntryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/colours")
public class PublicColourController {

    private final ColourPaletteEntryRepository repository;

    public PublicColourController(ColourPaletteEntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<PublicColourResponse> list() {
        return repository.findAllByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(colour -> new PublicColourResponse(
                        colour.getId(), colour.getName(), colour.getHex(), colour.getReseneUrl(),
                        colour.getDisplayOrder(), colour.isDefaultInterior(), colour.isDefaultExterior()
                ))
                .toList();
    }
}
