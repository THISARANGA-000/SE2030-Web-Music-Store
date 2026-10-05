package com.melodymart.albumcatalog.pricing;

import java.math.BigDecimal;

/**
 * Decorator Pattern — Concrete Component
 *
 * Wraps an album's original price from the database.
 * This is the base object that decorators wrap around.
 */
public class BaseAlbumPrice implements PricingComponent {

    private final BigDecimal originalPrice;

    public BaseAlbumPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    @Override
    public BigDecimal getPrice() {
        return originalPrice;
    }
}
