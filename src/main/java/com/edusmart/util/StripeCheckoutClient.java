package com.edusmart.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal Stripe Checkout Session client (form-encoded) without extra dependencies.
 * Docs: <a href="https://docs.stripe.com/api/checkout/sessions/create">Checkout Sessions</a>
 */
public final class StripeCheckoutClient {

    private static final String API_BASE = "https://api.stripe.com/v1";
    private static final Pattern URL_JSON = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ID_JSON = Pattern.compile("\"id\"\\s*:\\s*\"(cs_[^\"]+)\"");

    private StripeCheckoutClient() {}

    public static CheckoutSession createPaymentSessionEur(
            long totalAmountCents,
            String productName,
            String successUrl,
            String cancelUrl,
            String clientReferenceId) {

        if (totalAmountCents <= 0) {
            throw new IllegalArgumentException("Le montant doit être > 0 (centimes).");
        }
        String secret = StripeKeys.secretKey();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY manquante (variable d’environnement).");
        }

        Map<String, String> form = new java.util.LinkedHashMap<>();
        form.put("mode", "payment");
        form.put("success_url", successUrl);
        form.put("cancel_url", cancelUrl);
        if (clientReferenceId != null && !clientReferenceId.isBlank()) {
            form.put("client_reference_id", clientReferenceId);
        }

        // line_items[0] single charge for the final order total
        String safeName = (productName == null || productName.isBlank()) ? "EduSmart - Commande" : productName;
        form.put("line_items[0][quantity]", "1");
        form.put("line_items[0][price_data][currency]", "eur");
        form.put("line_items[0][price_data][unit_amount]", String.valueOf(totalAmountCents));
        form.put("line_items[0][price_data][product_data][name]", safeName);

        String body = encodeForm(form);
        String basic = Base64.getEncoder().encodeToString((secret + ":").getBytes(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder(URI.create(API_BASE + "/checkout/sessions"))
            .header("Authorization", "Basic " + basic)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        try {
            HttpResponse<String> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new IOException("Stripe HTTP " + resp.statusCode() + ": " + resp.body());
            }
            return parseSession(resp.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Requête Stripe interrompue", e);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d’appeler Stripe", e);
        }
    }

    private static CheckoutSession parseSession(String json) {
        Objects.requireNonNull(json, "json");
        String url = firstMatch(URL_JSON, json);
        String id = firstMatch(ID_JSON, json);
        if (url == null) {
            throw new IllegalStateException("Réponse Stripe inattendue (pas d’URL de session).");
        }
        return new CheckoutSession(id, url);
    }

    private static String firstMatch(Pattern p, String s) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1) : null;
    }

    private static String encodeForm(Map<String, String> fields) {
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (e.getValue() == null) continue;
            joiner.add(urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()));
        }
        return joiner.toString();
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public record CheckoutSession(String id, String url) {}
}
