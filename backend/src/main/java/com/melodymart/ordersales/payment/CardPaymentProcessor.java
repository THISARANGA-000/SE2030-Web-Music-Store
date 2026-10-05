package com.melodymart.ordersales.payment;

import java.math.BigDecimal;

/**
 * Factory Method Pattern — Concrete Product
 *
 * Represents a card-based payment processor (Credit/Debit Card).
 * Follows the same PaymentProcessor contract as DemoPaymentProcessor.
 */
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public String processPayment(Integer orderId, BigDecimal amount) {
        // In production this would call a real card gateway.
        // For now it generates a card transaction reference.
        return "CARD-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 900 + 100);
    }

    @Override
    public String getMethodName() {
        return "Credit Card";
    }
}
