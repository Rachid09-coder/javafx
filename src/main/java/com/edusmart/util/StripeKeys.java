package com.edusmart.util;

public final class StripeKeys {

    private StripeKeys() {
    }

    public static String getPublishableKey() {
        String value = System.getenv("STRIPE_PUBLISHABLE_KEY");
        return value == null ? "" : value.trim();
    }

    public static String getSecretKey() {
        String value = System.getenv("STRIPE_SECRET_KEY");
        return value == null ? "" : value.trim();
    }

    public static boolean isConfigured() {
        return !getPublishableKey().isEmpty() && !getSecretKey().isEmpty();
    }
}