package com.melodymart.ordersales.payment;

import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern — Factory
 *
 * Creates the appropriate PaymentProcessor based on the payment method string.
 * The checkout service calls this factory instead of directly instantiating
 * a concrete processor, decoupling the checkout logic from payment implementations.
 */
@Component
public class PaymentProcessorFactory {

    /**
     * Factory Method: returns the correct PaymentProcessor for the given method name.
     *
     * @param paymentMethod the payment method string from the checkout form
     * @return a PaymentProcessor implementation
     */
    public PaymentProcessor create(String paymentMethod) {
        if (paymentMethod != null && paymentMethod.toLowerCase().contains("card")) {
            return new CardPaymentProcessor();
        }
        // Default: Demo Payment (used by the existing checkout flow)
        return new DemoPaymentProcessor();
    }
}
