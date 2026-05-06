package com.edusmart.service.impl;

import com.edusmart.model.Course;
import com.edusmart.model.Subscriber;
import com.edusmart.service.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.List;
import java.util.Properties;

public class EmailServiceImpl implements EmailService {

    private final String username;
    private final String password;
    private final String host;
    private final String port;
    private final Properties props;

    public EmailServiceImpl() {
        this.username = System.getenv("SMTP_USERNAME");
        this.password = System.getenv("SMTP_PASSWORD");
        this.host = System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com");
        this.port = System.getenv().getOrDefault("SMTP_PORT", "587");

        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
    }

    @Override
    public void sendEmail(String to, String subject, String content) {
        System.out.println("[EmailService] Attempting to send email to: " + to);
        System.out.println("[EmailService] Using SMTP account: " + username);
        System.out.println("[EmailService] SMTP host: " + props.getProperty("mail.smtp.host")
                + " port: " + props.getProperty("mail.smtp.port"));

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            System.err.println("[EmailService] SMTP_USERNAME/SMTP_PASSWORD non définis. Envoi annulé.");
            return;
        }

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(true); // logs the full SMTP handshake to stdout

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(content, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("[EmailService] ✅ Email sent successfully to: " + to);

        } catch (MessagingException e) {
            System.err.println("[EmailService] ❌ Failed to send email to " + to
                    + " — Cause: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendPriceDropNotification(Course course, double oldPrice, List<Subscriber> subscribers) {
        System.out.println("[EmailService] sendPriceDropNotification called for course '"
                + course.getTitle() + "' — oldPrice=" + oldPrice + ", newPrice=" + course.getPrice()
                + ", subscribers=" + subscribers.size());

        String subject = "Price Drop Alert 🚀: " + course.getTitle();

        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;\">"
                + "<h2 style=\"color: #4F46E5;\">Good news! 🎉</h2>"
                + "<p>The course <strong>\"" + course.getTitle() + "\"</strong> has dropped in price.</p>"
                + "<div style=\"background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 20px 0;\">"
                + "<p style=\"margin: 5px 0; color: #64748b; text-decoration: line-through;\">Old Price: " + String.format("%.2f", oldPrice) + " DT</p>"
                + "<p style=\"margin: 5px 0; font-size: 18px; font-weight: bold; color: #10b981;\">New Price: " + String.format("%.2f", course.getPrice()) + " DT</p>"
                + "</div>"
                + "<p>Check it out now and start learning!</p>"
                + "<br/><p style=\"font-size: 12px; color: #94a3b8;\">You received this email because you subscribed to EduSmart notifications.</p>"
                + "</div>";

        for (Subscriber sub : subscribers) {
            System.out.println("[EmailService] Spawning thread to notify: " + sub.getEmail());
            new Thread(() -> sendEmail(sub.getEmail(), subject, htmlContent)).start();
        }
    }
}
