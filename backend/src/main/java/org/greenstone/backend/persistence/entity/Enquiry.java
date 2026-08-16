package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "enquiries")
public class Enquiry extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EnquiryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EnquiryStatus status = EnquiryStatus.NEW;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 40)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_preference", nullable = false, length = 20)
    private ContactPreference contactPreference = ContactPreference.EITHER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceOffering service;

    @Column(name = "property_address", length = 300)
    private String propertyAddress;

    @Column(length = 100)
    private String suburb;

    @Column(nullable = false, length = 10000)
    private String message;

    @Column(name = "estimated_budget", precision = 12, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(name = "desired_start_date")
    private LocalDate desiredStartDate;

    @Column(name = "internal_notes", length = 10000)
    private String internalNotes;

    protected Enquiry() {
    }

    public Enquiry(EnquiryType type, String firstName, String lastName, String email, String message) {
        this.type = type;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.message = message;
    }

    public EnquiryType getType() { return type; }
    public void setType(EnquiryType type) { this.type = type; }
    public EnquiryStatus getStatus() { return status; }
    public void setStatus(EnquiryStatus status) { this.status = status; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public ContactPreference getContactPreference() { return contactPreference; }
    public void setContactPreference(ContactPreference contactPreference) { this.contactPreference = contactPreference; }
    public ServiceOffering getService() { return service; }
    public void setService(ServiceOffering service) { this.service = service; }
    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }
    public String getSuburb() { return suburb; }
    public void setSuburb(String suburb) { this.suburb = suburb; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public BigDecimal getEstimatedBudget() { return estimatedBudget; }
    public void setEstimatedBudget(BigDecimal estimatedBudget) { this.estimatedBudget = estimatedBudget; }
    public LocalDate getDesiredStartDate() { return desiredStartDate; }
    public void setDesiredStartDate(LocalDate desiredStartDate) { this.desiredStartDate = desiredStartDate; }
    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
}
