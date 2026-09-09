package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "quotes")
public class Quote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private Enquiry enquiry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_admin_id", nullable = false)
    private AdminUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by_admin_id", nullable = false)
    private AdminUser updatedBy;

    @Column(name = "quote_number", nullable = false, unique = true, length = 40)
    private String quoteNumber;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @Version
    @Column(nullable = false)
    private long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 254)
    private String customerEmail;

    @Column(name = "property_address", length = 300)
    private String propertyAddress;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String scope;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String terms;

    @Column(name = "gst_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal gstRate = new BigDecimal("0.1500");

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "gst_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "optional_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal optionalTotal = BigDecimal.ZERO;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(name = "estimated_start_date")
    private LocalDate estimatedStartDate;

    @Column(name = "estimated_end_date")
    private LocalDate estimatedEndDate;

    @Column(name = "response_token_hash", length = 64)
    private String responseTokenHash;

    @Column(name = "response_token_expires_at")
    private OffsetDateTime responseTokenExpiresAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "declined_at")
    private OffsetDateTime declinedAt;

    @Column(name = "decline_reason", length = 1000)
    private String declineReason;

    @Column(name = "content_updated_at")
    private OffsetDateTime contentUpdatedAt;

    protected Quote() {
    }

    public Quote(Enquiry enquiry, AdminUser actor, String quoteNumber, int revisionNumber) {
        this.enquiry = enquiry;
        this.createdBy = actor;
        this.updatedBy = actor;
        this.quoteNumber = quoteNumber;
        this.revisionNumber = revisionNumber;
    }

    public Enquiry getEnquiry() { return enquiry; }
    public AdminUser getCreatedBy() { return createdBy; }
    public AdminUser getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(AdminUser updatedBy) { this.updatedBy = updatedBy; }
    public String getQuoteNumber() { return quoteNumber; }
    public int getRevisionNumber() { return revisionNumber; }
    public long getVersion() { return version; }
    public QuoteStatus getStatus() { return status; }
    public void setStatus(QuoteStatus status) { this.status = status; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }
    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getGstAmount() { return gstAmount; }
    public void setGstAmount(BigDecimal gstAmount) { this.gstAmount = gstAmount; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getOptionalTotal() { return optionalTotal; }
    public void setOptionalTotal(BigDecimal optionalTotal) { this.optionalTotal = optionalTotal; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public LocalDate getEstimatedStartDate() { return estimatedStartDate; }
    public void setEstimatedStartDate(LocalDate estimatedStartDate) { this.estimatedStartDate = estimatedStartDate; }
    public LocalDate getEstimatedEndDate() { return estimatedEndDate; }
    public void setEstimatedEndDate(LocalDate estimatedEndDate) { this.estimatedEndDate = estimatedEndDate; }
    public String getResponseTokenHash() { return responseTokenHash; }
    public void setResponseTokenHash(String responseTokenHash) { this.responseTokenHash = responseTokenHash; }
    public OffsetDateTime getResponseTokenExpiresAt() { return responseTokenExpiresAt; }
    public void setResponseTokenExpiresAt(OffsetDateTime responseTokenExpiresAt) { this.responseTokenExpiresAt = responseTokenExpiresAt; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
    public OffsetDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(OffsetDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public OffsetDateTime getDeclinedAt() { return declinedAt; }
    public void setDeclinedAt(OffsetDateTime declinedAt) { this.declinedAt = declinedAt; }
    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }
    public void touchContent(OffsetDateTime now) { this.contentUpdatedAt = now; }
}
