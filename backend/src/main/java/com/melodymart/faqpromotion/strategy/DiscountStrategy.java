package com.melodymart.faqpromotion.strategy;

import java.math.BigDecimal;

/**
 * Strategy Pattern — Strategy interface
 *
 * Defines the algorithm contract for calculating a discounted price.
 * Different promotion types use different concrete strategies.
 */
public interface DiscountStrategy {

    /**
     * Calculate the discounted price.
     *
     * @param originalPrice the album's original price
     * @param discountValue the discount value from the PROMOTION table
     * @return the final price after the discount is applied
     */
    BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, BigDecimal discountValue);
}
