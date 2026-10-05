package com.melodymart.albumcatalog.pricing;

import java.math.BigDecimal;

/**
 * Decorator Pattern — Component (interface)
 *
 * Defines the pricing contract implemented by both the concrete component
 * (BaseAlbumPrice) and all decorators (PromotionPriceDecorator).
 */
public interface PricingComponent {

    /**
     * Returns the final price for this album, after any applied decorators.
     */
    BigDecimal getPrice();
}
