package com.melodymart.albumcatalog.pricing;

import com.melodymart.faqpromotion.strategy.DiscountStrategy;

import java.math.BigDecimal;

/**
 * Decorator Pattern — Concrete Decorator
 *
 * Wraps any PricingComponent and dynamically adds promotion/discount behavior.
 * It delegates to the wrapped component for the base price, then applies
 * a DiscountStrategy (Strategy Pattern) to compute the final price.
 *
 * This allows discounts to be layered on top of a base price without
 * modifying the Album entity or BaseAlbumPrice class.
 */
public class PromotionPriceDecorator implements PricingComponent {

    // Decorator Pattern — holds a reference to the wrapped component
    private final PricingComponent wrapped;

    // Strategy Pattern — the discount algorithm to apply
    private final DiscountStrategy discountStrategy;
    private final BigDecimal discountValue;

    public PromotionPriceDecorator(PricingComponent wrapped,
                                   DiscountStrategy discountStrategy,
                                   BigDecimal discountValue) {
        this.wrapped = wrapped;
        this.discountStrategy = discountStrategy;
        this.discountValue = discountValue;
    }

    @Override
    public BigDecimal getPrice() {
        // Delegate to the wrapped component, then apply the discount strategy
        BigDecimal basePrice = wrapped.getPrice();
        return discountStrategy.calculateDiscountedPrice(basePrice, discountValue);
    }
}
