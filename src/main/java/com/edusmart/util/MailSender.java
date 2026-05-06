package com.edusmart.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.Properties;

/**
 * Sends EduSmart branded emails with optional PDF attachments.
 *
 * <p>SMTP is configured for Gmail (App Password required).
 * Replace {@link #USERNAME} / {@link #PASSWORD} with your credentials, or
 * inject them via environment variables for production use.
 */
public class MailSender {

    // ── SMTP configuration ──────────────────────────────────────────────────
    private static final String HOST     = "smtp.gmail.com";
    private static final String PORT     = "587";
    // Read from environment variables; fall back to defaults only for local development.
    private static final String USERNAME = getEnv("EDUSMART_MAIL_USER", "YOUR_EMAIL@gmail.com");
    private static final String PASSWORD = getEnv("EDUSMART_MAIL_PASS", "YOUR_APP_PASSWORD");

    // ── Brand colours (kept in sync with CSS vars) ───────────────────────────
    private static final String C_BLUE      = "#1E3A8A";
    private static final String C_BLUE_LT   = "#2563EB";
    private static final String C_PURPLE    = "#7C3AED";
    private static final String C_BG        = "#F8FAFC";
    private static final String C_TEXT      = "#1E293B";
    private static final String C_MUTED     = "#64748B";
    private static final String C_BORDER    = "#E2E8F0";
    private static final String C_SUCCESS   = "#10B981";

    // ═══════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Sends an HTML email with an optional PDF attachment.
     *
     * @param to         recipient address
     * @param subject    email subject
     * @param htmlBody   pre-built HTML body (use {@link #buildBulletinEmailBody} or
     *                   {@link #buildCertificationEmailBody} helpers)
     * @param attachment PDF file to attach, or {@code null}
     */
    public static void sendEmailWithAttachment(String to, String subject,
                                               String htmlBody, File attachment)
            throws MessagingException {
        Session session = buildSession();

        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(USERNAME, "EduSmart", "UTF-8"));
        } catch (java.io.UnsupportedEncodingException uee) {
            message.setFrom(new InternetAddress(USERNAME));
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);

        if (attachment != null && attachment.exists()) {
            MimeBodyPart attachPart = new MimeBodyPart();
            try {
                attachPart.attachFile(attachment);
                multipart.addBodyPart(attachPart);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        message.setContent(multipart);
        Transport.send(message);
    }

    // ── Convenience overload that accepts a plain-text body ──────────────────
    /**
     * Convenience method kept for backward compatibility.
     * Wraps a plain-text body in a minimal HTML template before sending.
     */
    public static void sendEmailWithAttachment(String to, String subject,
                                               String plainBody, File attachment,
                                               boolean legacyPlainText)
            throws MessagingException {
        String htmlBody = legacyPlainText
                ? wrapPlainText(subject, plainBody)
                : plainBody;
        sendEmailWithAttachment(to, subject, htmlBody, attachment);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HTML TEMPLATE BUILDERS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Builds a professional HTML email body for a Bulletin notification.
     */
    public static String buildBulletinEmailBody(String studentName,
                                                String academicYear,
                                                String semester,
                                                String average,
                                                String mention,
                                                String rank) {
        String content = "<p style='font-size:16px;color:" + C_TEXT + ";margin:0 0 20px 0;'>"
                + "Bonjour <strong>" + esc(studentName) + "</strong>,</p>"
                + "<p style='color:" + C_MUTED + ";margin:0 0 24px 0;'>Votre bulletin de notes pour l'année académique "
                + "<strong style='color:" + C_BLUE + ";'>" + esc(academicYear) + "</strong>"
                + " — semestre <strong>" + esc(semester) + "</strong> est maintenant disponible.</p>"
                + buildResultCard("Moyenne générale", average, C_BLUE_LT)
                + buildResultCard("Mention",          mention,  C_PURPLE)
                + buildResultCard("Classement",       rank,     C_SUCCESS)
                + "<p style='color:" + C_MUTED + ";font-size:13px;margin-top:28px;'>"
                + "Le document PDF est joint à cet email. Vous pouvez également vous connecter à votre "
                + "espace EduSmart pour le consulter en ligne.</p>";
        return wrapTemplate("Votre Bulletin de Notes", content, "📄 Bulletin de Notes");
    }

    /**
     * Builds a professional HTML email body for a Certification notification.
     */
    public static String buildCertificationEmailBody(String studentName,
                                                     String certType,
                                                     String issuedDate,
                                                     String validUntil,
                                                     String uniqueNumber) {
        String content = "<p style='font-size:16px;color:" + C_TEXT + ";margin:0 0 20px 0;'>"
                + "Félicitations <strong>" + esc(studentName) + "</strong> ! 🎉</p>"
                + "<p style='color:" + C_MUTED + ";margin:0 0 24px 0;'>Votre certification "
                + "<strong style='color:" + C_PURPLE + ";'>" + esc(certType) + "</strong>"
                + " a été émise avec succès.</p>"
                + buildResultCard("Date d'émission",     issuedDate,   C_BLUE)
                + buildResultCard("Valide jusqu'au",     validUntil,   C_BLUE_LT)
                + buildResultCard("Numéro unique",       uniqueNumber, C_PURPLE)
                + "<p style='color:" + C_MUTED + ";font-size:13px;margin-top:28px;'>"
                + "Le certificat PDF officiel est joint à cet email. "
                + "Scannez le QR code sur le document pour vérifier son authenticité sur <strong>edusmart.com</strong>.</p>";
        return wrapTemplate("Votre Certification EduSmart", content, "🏆 Certification EduSmart");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private static Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            HOST);
        props.put("mail.smtp.port",            PORT);
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    /** Wraps the main content section inside the full EduSmart HTML email shell. */
    private static String wrapTemplate(String title, String bodyContent, String headerLabel) {
        return "<!DOCTYPE html>"
                + "<html lang='fr'><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>" + esc(title) + "</title></head>"
                + "<body style='margin:0;padding:0;background-color:" + C_BG + ";font-family:Arial,Helvetica,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:" + C_BG + ";padding:32px 0;'>"
                + "<tr><td align='center'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='background:#fff;"
                +     "border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(30,58,138,.10);'>"

                // Header band
                + "<tr><td style='background:" + C_BLUE + ";padding:32px 40px 24px;'>"
                +   "<div style='font-size:28px;font-weight:700;color:#fff;letter-spacing:-0.5px;'>EduSmart</div>"
                +   "<div style='font-size:12px;color:rgba(255,255,255,.7);margin-top:4px;'>Plateforme Éducative Intelligente</div>"
                + "</td></tr>"
                // Purple accent strip
                + "<tr><td style='background:" + C_PURPLE + ";height:5px;'></td></tr>"

                // Main content
                + "<tr><td style='padding:36px 40px;'>"
                +   "<h2 style='margin:0 0 24px 0;font-size:22px;color:" + C_BLUE + ";'>" + esc(headerLabel) + "</h2>"
                +   bodyContent
                + "</td></tr>"

                // Divider
                + "<tr><td style='padding:0 40px;'>"
                +   "<hr style='border:none;border-top:1px solid " + C_BORDER + ";margin:0;'/>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='padding:24px 40px;text-align:center;'>"
                +   "<p style='color:" + C_MUTED + ";font-size:12px;margin:0;'>"
                +     "© 2024 EduSmart — Tous droits réservés"
                +   "</p>"
                +   "<p style='color:" + C_MUTED + ";font-size:11px;margin:6px 0 0 0;'>"
                +     "Cet email a été généré automatiquement, merci de ne pas y répondre."
                +   "</p>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr></table>"
                + "</body></html>";
    }

    /** Builds a small coloured result/info card row. */
    private static String buildResultCard(String label, String value, String accentColor) {
        return "<table width='100%' cellpadding='0' cellspacing='0' style='margin-bottom:12px;border-radius:8px;overflow:hidden;'>"
                + "<tr>"
                +   "<td style='background:" + accentColor + ";padding:10px 16px;width:40%;'>"
                +     "<span style='color:#fff;font-size:13px;font-weight:700;'>" + esc(label) + "</span>"
                +   "</td>"
                +   "<td style='background:" + C_BG + ";padding:10px 16px;border:1px solid " + C_BORDER + ";border-left:none;border-radius:0 8px 8px 0;'>"
                +     "<span style='color:" + C_TEXT + ";font-size:13px;font-weight:600;'>" + esc(value) + "</span>"
                +   "</td>"
                + "</tr>"
                + "</table>";
    }

    /** Minimal HTML wrapper for plain-text fallback. */
    private static String wrapPlainText(String title, String body) {
        String htmlBody = "<p style='white-space:pre-line;color:#1E293B;font-family:Arial,sans-serif;'>" + esc(body) + "</p>";
        return wrapTemplate(title, htmlBody, title);
    }

    /** Basic HTML escaping for user-supplied strings. */
    private static String esc(String s) {
        if (s == null) return "N/A";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /** Returns the environment variable {@code name}, or {@code fallback} if not set. */
    private static String getEnv(String name, String fallback) {
        String val = System.getenv(name);
        return (val != null && !val.isBlank()) ? val : fallback;
    }
}
