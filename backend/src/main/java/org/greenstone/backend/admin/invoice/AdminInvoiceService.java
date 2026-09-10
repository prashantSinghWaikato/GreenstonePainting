package org.greenstone.backend.admin.invoice;

import org.greenstone.backend.persistence.entity.*;
import org.greenstone.backend.persistence.repository.*;
import org.greenstone.backend.web.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class AdminInvoiceService {
    private final InvoiceRepository invoices; private final PaintingJobRepository jobs; private final AdminUserRepository users; private final JobActivityRepository activities; private final InvoicePdfService pdf;
    public AdminInvoiceService(InvoiceRepository i,PaintingJobRepository j,AdminUserRepository u,JobActivityRepository a,InvoicePdfService p){invoices=i;jobs=j;users=u;activities=a;pdf=p;}
    @Transactional(readOnly=true) public List<AdminInvoiceResponse> list(){return invoices.findAllByOrderByCreatedAtDesc().stream().map(this::response).toList();}
    @Transactional public AdminInvoiceResponse create(UUID jobId,String email){var existing=invoices.findByJobId(jobId);if(existing.isPresent())return response(existing.get());var job=job(jobId);if(job.getStatus()!=JobStatus.COMPLETED)throw new WorkflowConflictException("Complete the job before creating its invoice.");var actor=actor(email);var q=job.getQuote();var invoice=invoices.saveAndFlush(new Invoice(job,number(),actor,q.getSubtotal(),q.getGstAmount(),q.getTotal(),LocalDate.now().plusDays(14)));activities.save(new JobActivity(job,actor,JobActivityType.INVOICE_CREATED,"Invoice "+invoice.getInvoiceNumber()+" created.",null));return response(invoice);}
    @Transactional(readOnly=true) public AdminInvoiceResponse find(UUID id){return response(invoice(id));}
    @Transactional public AdminInvoiceResponse update(UUID id,UpdateInvoiceRequest r,String email){var i=invoice(id);if(i.getVersion()!=r.version())throw new WorkflowConflictException("This invoice was updated by another staff member. Reload it before saving again.");if(r.amountPaid().compareTo(i.getTotal())>0)throw new JobValidationException("Amount paid cannot exceed the invoice total.");if(i.getStatus()==InvoiceStatus.VOID&&r.status()!=InvoiceStatus.VOID)throw new WorkflowConflictException("A void invoice cannot be reopened.");var now=OffsetDateTime.now(ZoneOffset.UTC);var status=r.status();if(status!=InvoiceStatus.VOID){if(r.amountPaid().compareTo(i.getTotal())==0)status=InvoiceStatus.PAID;else if(r.amountPaid().signum()>0)status=InvoiceStatus.PART_PAID;else if(status==InvoiceStatus.PAID||status==InvoiceStatus.PART_PAID)throw new JobValidationException("Record a payment amount before using a paid status.");}i.setStatus(status);i.setAmountPaid(r.amountPaid().setScale(2,RoundingMode.HALF_UP));i.setDueDate(r.dueDate());i.setPaymentReference(normalize(r.paymentReference()));i.setNotes(normalize(r.notes()));i.setUpdatedBy(actor(email));if(status!=InvoiceStatus.DRAFT&&status!=InvoiceStatus.VOID&&i.getSentAt()==null)i.setSentAt(now);if(status==InvoiceStatus.PAID)i.setPaidAt(now);invoices.flush();return response(i);}
    @Transactional(readOnly=true) public byte[] pdf(UUID id){return pdf.generate(invoice(id));}
    private Invoice invoice(UUID id){return invoices.findById(id).orElseThrow(()->new ResourceNotFoundException("Invoice was not found."));} private PaintingJob job(UUID id){return jobs.findById(id).orElseThrow(()->new ResourceNotFoundException("Job was not found."));} private AdminUser actor(String e){return users.findByEmailIgnoreCase(e).orElseThrow(()->new ResourceNotFoundException("Staff account is unavailable."));}
    private String normalize(String v){return v==null||v.isBlank()?null:v.trim();} private String number(){return "INV-"+LocalDate.now().getYear()+"-"+UUID.randomUUID().toString().substring(0,6).toUpperCase(Locale.ROOT);}
    private AdminInvoiceResponse response(Invoice i){var j=i.getJob();return new AdminInvoiceResponse(i.getId(),j.getId(),j.getJobNumber(),i.getInvoiceNumber(),i.getVersion(),i.getStatus(),j.getCustomerName(),j.getCustomerEmail(),j.getPropertyAddress(),i.getSubtotal(),i.getGstAmount(),i.getTotal(),i.getAmountPaid(),i.getTotal().subtract(i.getAmountPaid()),i.getDueDate(),i.getSentAt(),i.getPaidAt(),i.getPaymentReference(),i.getNotes(),i.getCreatedAt());}
}
