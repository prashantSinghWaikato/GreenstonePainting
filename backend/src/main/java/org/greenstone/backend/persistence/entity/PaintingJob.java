package org.greenstone.backend.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "painting_jobs")
public class PaintingJob extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "quote_id", nullable = false, unique = true)
    private Quote quote;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "enquiry_id", nullable = false)
    private Enquiry enquiry;
    @Column(name = "job_number", nullable = false, unique = true, length = 40) private String jobNumber;
    @Version @Column(nullable = false) private long version;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private JobStatus status = JobStatus.PLANNED;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_admin_id") private AdminUser assignedTo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by_admin_id", nullable = false) private AdminUser createdBy;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "updated_by_admin_id", nullable = false) private AdminUser updatedBy;
    @Column(name = "customer_name", nullable = false, length = 200) private String customerName;
    @Column(name = "customer_email", nullable = false, length = 254) private String customerEmail;
    @Column(name = "customer_phone", length = 40) private String customerPhone;
    @Column(name = "property_address", length = 300) private String propertyAddress;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "service_title", length = 200) private String serviceTitle;
    @Column(nullable = false, columnDefinition = "TEXT") private String scope;
    @Column(name = "site_instructions", columnDefinition = "TEXT") private String siteInstructions;
    @Column(name = "internal_notes", columnDefinition = "TEXT") private String internalNotes;
    @Column(name = "scheduled_start_date") private LocalDate scheduledStartDate;
    @Column(name = "scheduled_end_date") private LocalDate scheduledEndDate;
    @Column(name = "actual_started_at") private OffsetDateTime actualStartedAt;
    @Column(name = "completed_at") private OffsetDateTime completedAt;
    @Column(name = "customer_signoff_name", length = 200) private String customerSignoffName;
    @Column(name = "customer_signoff_at") private OffsetDateTime customerSignoffAt;

    protected PaintingJob() {}
    public PaintingJob(Quote quote, AdminUser actor, String jobNumber) { this.quote = quote; this.enquiry = quote.getEnquiry(); this.createdBy = actor; this.updatedBy = actor; this.jobNumber = jobNumber; }
    public Quote getQuote() { return quote; } public Enquiry getEnquiry() { return enquiry; } public String getJobNumber() { return jobNumber; }
    public long getVersion() { return version; } public JobStatus getStatus() { return status; } public void setStatus(JobStatus status) { this.status = status; }
    public AdminUser getAssignedTo() { return assignedTo; } public void setAssignedTo(AdminUser assignedTo) { this.assignedTo = assignedTo; }
    public AdminUser getCreatedBy() { return createdBy; } public AdminUser getUpdatedBy() { return updatedBy; } public void setUpdatedBy(AdminUser updatedBy) { this.updatedBy = updatedBy; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String value) { customerName = value; }
    public String getCustomerEmail() { return customerEmail; } public void setCustomerEmail(String value) { customerEmail = value; }
    public String getCustomerPhone() { return customerPhone; } public void setCustomerPhone(String value) { customerPhone = value; }
    public String getPropertyAddress() { return propertyAddress; } public void setPropertyAddress(String value) { propertyAddress = value; }
    public String getTitle() { return title; } public void setTitle(String value) { title = value; }
    public String getServiceTitle() { return serviceTitle; } public void setServiceTitle(String value) { serviceTitle = value; }
    public String getScope() { return scope; } public void setScope(String value) { scope = value; }
    public String getSiteInstructions() { return siteInstructions; } public void setSiteInstructions(String value) { siteInstructions = value; }
    public String getInternalNotes() { return internalNotes; } public void setInternalNotes(String value) { internalNotes = value; }
    public LocalDate getScheduledStartDate() { return scheduledStartDate; } public void setScheduledStartDate(LocalDate value) { scheduledStartDate = value; }
    public LocalDate getScheduledEndDate() { return scheduledEndDate; } public void setScheduledEndDate(LocalDate value) { scheduledEndDate = value; }
    public OffsetDateTime getActualStartedAt() { return actualStartedAt; } public void setActualStartedAt(OffsetDateTime value) { actualStartedAt = value; }
    public OffsetDateTime getCompletedAt() { return completedAt; } public void setCompletedAt(OffsetDateTime value) { completedAt = value; }
    public String getCustomerSignoffName(){return customerSignoffName;} public void setCustomerSignoffName(String value){customerSignoffName=value;} public OffsetDateTime getCustomerSignoffAt(){return customerSignoffAt;} public void setCustomerSignoffAt(OffsetDateTime value){customerSignoffAt=value;}
}
