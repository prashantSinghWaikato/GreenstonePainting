package org.greenstone.backend.admin.content;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/content/colours")
public class AdminColourController {

    private final AdminColourService service;

    public AdminColourController(AdminColourService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminColourResponse> list() { return service.list(); }

    @PostMapping
    public AdminColourResponse create(@Valid @RequestBody SaveAdminColourRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{colourId}")
    public AdminColourResponse update(@PathVariable UUID colourId, @Valid @RequestBody SaveAdminColourRequest request) {
        return service.update(colourId, request);
    }

    @DeleteMapping("/{colourId}")
    public ResponseEntity<Void> delete(@PathVariable UUID colourId) {
        service.delete(colourId);
        return ResponseEntity.noContent().build();
    }
}
