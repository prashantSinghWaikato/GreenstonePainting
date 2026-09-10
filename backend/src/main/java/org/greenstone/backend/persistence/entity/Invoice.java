package org.greenstone.backend.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "job_id", nullable = false, unique = true) private PaintingJob job;
    @Column(name = "invoice_number", nullable = false, unique = true, length = 40) private String invoiceNumber;
    @Version @Column(nullable = false) private long version;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private InvoiceStatus status = InvoiceStatus.DRAFT;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Column(name = "gst_amount", nullable = false, precision = 12, scale = 2) private BigDecimal gstAmount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2) private BigDecimal amountPaid = BigDecimal.ZERO;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(name = "sent_at") private OffsetDateTime sentAt;
    @Column(name = "paid_at") private OffsetDateTime paidAt;
    @Column(name = "payment_reference", length = 200) private String paymentReference;
    @Column(columnDefinition = "TEXT") private String notes;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by_admin_id", nullable = false) private AdminUser createdBy;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "updated_by_admin_id", nullable = false) private AdminUser updatedBy;
    protected Invoice() {}
    public Invoice(PaintingJob job, String number, AdminUser actor, BigDecimal subtotal, BigDecimal gst, BigDecimal total, LocalDate due) { this.job=job; invoiceNumber=number; createdBy=actor; updatedBy=actor; this.subtotal=subtotal; gstAmount=gst; this.total=total; dueDate=due; }
    public PaintingJob getJob(){return job;} public String getInvoiceNumber(){return invoiceNumber;} public long getVersion(){return version;} public InvoiceStatus getStatus(){return status;} public void setStatus(InvoiceStatus v){status=v;}
    public BigDecimal getSubtotal(){return subtotal;} public BigDecimal getGstAmount(){return gstAmount;} public BigDecimal getTotal(){return total;} public BigDecimal getAmountPaid(){return amountPaid;} public void setAmountPaid(BigDecimal v){amountPaid=v;}
    public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;} public OffsetDateTime getSentAt(){return sentAt;} public void setSentAt(OffsetDateTime v){sentAt=v;} public OffsetDateTime getPaidAt(){return paidAt;} public void setPaidAt(OffsetDateTime v){paidAt=v;}
    public String getPaymentReference(){return paymentReference;} public void setPaymentReference(String v){paymentReference=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;} public void setUpdatedBy(AdminUser v){updatedBy=v;} public AdminUser getUpdatedBy(){return updatedBy;}
}
