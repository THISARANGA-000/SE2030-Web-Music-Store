package com.melodymart.ordersales.payment;

import java.math.BigDecimal;

/**
 * Factory Method Pattern — Product (interface)
 *
 * Defines the contract for all payment processors.
 * The checkout flow depends on this abstraction, not on concrete implementations.
 */
public interface PaymentProcessor {

    /**
     * Process a payment for the given order.
     *
     * @param orderId       the order being paid for
     * @param amount        the total amount to charge
     * @return a unique transaction reference string
     */
    String processPayment(Integer orderId, BigDecimal amount);

    /**
     * Returns the payment method name stored in the PAYMENT table.
     */
    String getMethodName();
}
