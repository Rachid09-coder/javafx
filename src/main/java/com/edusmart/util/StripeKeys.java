package com.edusmart.util;

/**
 * Stripe key resolution.
 *
 * <p><b>Never commit secret keys in source code.</b> Use environment variables, CI secrets, or
 * a local .env you keep out of git. See <a href="https://docs.stripe.com/api">Stripe API</a>
 * (authentication) for details.
 */
public final class StripeKeys {

    private StripeKeys() {}

    /** Secret key (server-side / privileged). */
    public static String secretKey() {
        String v = firstNonBlank(
            System.getenv("STRIPE_SECRET_KEY"),
            System.getProperty("stripe.secretKey")
        );
        return v != null ? v.trim() : null;
    }

    /**
     * Publishable key (client-side, safe to ship; used for Stripe.js / Elements, etc.).
     * Not required for hosted Checkout in many flows, but keep it in env for future use.
     */
    public static String publishableKey() {
        String v = firstNonBlank(
            System.getenv("STRIPE_PUBLISHABLE_KEY"),
            System.getProperty("stripe.publishableKey")
        );
        return v != null ? v.trim() : null;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
