package com.edusmart.util;

public class StripeCheckoutClient {

    public String createCheckoutUrl(double totalAmount) {
        if (!StripeKeys.isConfigured()) {
            return null;
        }
        return "https://checkout.stripe.com/pay/simulated?amount=" + String.format("%.2f", totalAmount);
    }
}