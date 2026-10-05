package com.melodymart.faqpromotion.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy Pattern — Concrete Strategy
 *
 * Applies a percentage-based discount.
 * Used when the PROMOTION table has DiscountType = 'Percentage'.
 *
 * Example: 20% off $10.00 → $8.00
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, BigDecimal discountValue) {
        BigDecimal discount = originalPrice
                .multiply(discountValue)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal result = originalPrice.subtract(discount);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result.setScale(2, RoundingMode.HALF_UP);
    }
}
