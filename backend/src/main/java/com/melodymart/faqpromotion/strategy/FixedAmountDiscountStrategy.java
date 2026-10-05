package com.melodymart.faqpromotion.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy Pattern — Concrete Strategy
 *
 * Applies a fixed-amount discount.
 * Used when the PROMOTION table has DiscountType = 'FixedAmount'.
 *
 * Example: $3.00 off $10.00 → $7.00
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, BigDecimal discountValue) {
        BigDecimal result = originalPrice.subtract(discountValue);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result.setScale(2, RoundingMode.HALF_UP);
    }
}
