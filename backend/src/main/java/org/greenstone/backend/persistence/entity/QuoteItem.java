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
import java.math.RoundingMode;

@Entity
@Table(name = "quote_items")
public class QuoteItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuoteItemCategory category;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, length = 40)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private boolean optional;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected QuoteItem() {
    }

    public QuoteItem(Quote quote, QuoteItemCategory category, String description, BigDecimal quantity,
                     String unit, BigDecimal unitPrice, boolean optional, int displayOrder) {
        this.quote = quote;
        this.category = category;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.optional = optional;
        this.displayOrder = displayOrder;
    }

    public Quote getQuote() { return quote; }
    public QuoteItemCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public boolean isOptional() { return optional; }
    public int getDisplayOrder() { return displayOrder; }
    public BigDecimal getLineTotal() { return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP); }
}
