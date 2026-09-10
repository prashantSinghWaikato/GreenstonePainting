package org.greenstone.backend.admin.job;

import jakarta.validation.Valid;
import org.greenstone.backend.persistence.entity.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminJobController {
    private final AdminJobService service;
    public AdminJobController(AdminJobService service) { this.service = service; }
    @GetMapping("/jobs") public AdminJobPageResponse list(@RequestParam(defaultValue = "") String q, @RequestParam(required = false) JobStatus status, @RequestParam(required = false) String assignment, Authentication auth) { return service.list(q, status, assignment, auth.getName()); }
    @GetMapping("/jobs/overview") public AdminJobOverviewResponse overview() { return service.overview(); }
    @PostMapping("/quotes/{quoteId}/job") public AdminJobDetailResponse create(@PathVariable UUID quoteId, Authentication auth) { return service.createFromQuote(quoteId, auth.getName()); }
    @GetMapping("/jobs/{jobId}") public AdminJobDetailResponse find(@PathVariable UUID jobId) { return service.find(jobId); }
    @PatchMapping("/jobs/{jobId}") public AdminJobDetailResponse update(@PathVariable UUID jobId, @Valid @RequestBody UpdateJobRequest request, Authentication auth) { return service.update(jobId, request, auth.getName()); }
    @PatchMapping("/jobs/{jobId}/checklist/{itemId}") public AdminJobDetailResponse checklist(@PathVariable UUID jobId, @PathVariable UUID itemId, @Valid @RequestBody UpdateChecklistRequest request, Authentication auth) { return service.updateChecklist(jobId, itemId, request, auth.getName()); }
    @PostMapping("/jobs/{jobId}/signoff") public AdminJobDetailResponse signoff(@PathVariable UUID jobId, @Valid @RequestBody RecordSignoffRequest request, Authentication auth) { return service.recordSignoff(jobId, request, auth.getName()); }
    @PostMapping(value = "/jobs/{jobId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) public AdminJobDetailResponse photo(@PathVariable UUID jobId, @RequestParam JobPhotoPhase phase, @RequestPart("file") MultipartFile file, Authentication auth) { return service.addPhoto(jobId, phase, file, auth.getName()); }
    @GetMapping("/jobs/{jobId}/photos/{photoId}") public ResponseEntity<org.springframework.core.io.Resource> photo(@PathVariable UUID jobId, @PathVariable UUID photoId) {
        var download = service.photo(jobId, photoId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.contentType())).contentLength(download.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(download.filename(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store").body(download.resource());
    }
}
