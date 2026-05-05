package com.edusmart.util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SmsSender {
    // Credentials loaded from environment variables for security
    public static final String ACCOUNT_SID = System.getenv().getOrDefault("TWILIO_ACCOUNT_SID", "");
    public static final String AUTH_TOKEN  = System.getenv().getOrDefault("TWILIO_AUTH_TOKEN", "");
    public static final String FROM_NUMBER = System.getenv().getOrDefault("TWILIO_FROM_NUMBER", "+13639991312");

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static boolean sendSms(String toNumber, String content) {
        if (ACCOUNT_SID.isEmpty() || AUTH_TOKEN.isEmpty()) {
            System.err.println("[SmsSender] Twilio credentials not set. Skipping SMS.");
            return false;
        }
        try {
            System.out.println("[SMS Sending] To: " + toNumber);

            String url = "https://api.twilio.com/2010-04-01/Accounts/" + ACCOUNT_SID + "/Messages.json";
            String credentials = Base64.getEncoder().encodeToString(
                    (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(StandardCharsets.UTF_8));

            String body = "To=" + URLEncoder.encode(toNumber, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(FROM_NUMBER, StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(content, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void notifyBulletin(String phone, String studentName, String semester, String year) {
        String msg = String.format("EduSmart - Bonjour %s, votre bulletin %s %s est prêt. Connectez-vous pour le voir.",
                studentName, semester, year);
        sendSms(phone, msg);
    }
}
