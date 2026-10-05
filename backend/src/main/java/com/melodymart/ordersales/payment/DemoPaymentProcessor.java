package com.melodymart.ordersales.payment;

import java.math.BigDecimal;

/**
 * Factory Method Pattern — Concrete Product
 *
 * Simulates an instant demo payment (no real gateway).
 * Corresponds to the "Demo Payment" method used in the existing checkout flow.
 */
public class DemoPaymentProcessor implements PaymentProcessor {

    @Override
    public String processPayment(Integer orderId, BigDecimal amount) {
        // Simulate instant approval; generate a transaction reference
        return "TXN-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 900 + 100);
    }

    @Override
    public String getMethodName() {
        return "Demo Payment";
    }
}
