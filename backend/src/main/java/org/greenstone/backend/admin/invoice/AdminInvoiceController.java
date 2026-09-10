package org.greenstone.backend.admin.invoice;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
@RestController @RequestMapping("/api/admin")
public class AdminInvoiceController {
 private final AdminInvoiceService service; public AdminInvoiceController(AdminInvoiceService s){service=s;}
 @GetMapping("/invoices") public List<AdminInvoiceResponse> list(){return service.list();}
 @PostMapping("/jobs/{jobId}/invoice") public AdminInvoiceResponse create(@PathVariable UUID jobId,Authentication a){return service.create(jobId,a.getName());}
 @GetMapping("/invoices/{id}") public AdminInvoiceResponse find(@PathVariable UUID id){return service.find(id);}
 @PatchMapping("/invoices/{id}") public AdminInvoiceResponse update(@PathVariable UUID id,@Valid @RequestBody UpdateInvoiceRequest r,Authentication a){return service.update(id,r,a.getName());}
 @GetMapping("/invoices/{id}/pdf") public ResponseEntity<byte[]> pdf(@PathVariable UUID id){var invoice=service.find(id);return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(invoice.invoiceNumber()+".pdf",StandardCharsets.UTF_8).build().toString()).body(service.pdf(id));}
}
