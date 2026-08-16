package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "testimonials")
public class Testimonial extends BaseEntity {

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(length = 150)
    private String location;

    @Column(nullable = false)
    private int rating;

    @Column(name = "quote_text", nullable = false, length = 3000)
    private String quoteText;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublicationStatus status = PublicationStatus.DRAFT;

    @Column(nullable = false)
    private boolean featured;

    protected Testimonial() {
    }

    public Testimonial(String customerName, int rating, String quoteText) {
        this.customerName = customerName;
        this.rating = rating;
        this.quoteText = quoteText;
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getQuoteText() { return quoteText; }
    public void setQuoteText(String quoteText) { this.quoteText = quoteText; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
}
