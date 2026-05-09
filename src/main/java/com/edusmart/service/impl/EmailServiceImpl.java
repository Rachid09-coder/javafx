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
    private final Properties props;

    public EmailServiceImpl() {
        // Fetch credentials from env variables or defaults (for demo purposes)
        this.username = System.getenv().getOrDefault("EDUSMART_EMAIL", "test@edusmart.com");
        this.password = System.getenv().getOrDefault("EDUSMART_PASSWORD", "password123");

        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com"); // default to Gmail
        props.put("mail.smtp.port", "587");
    }

    @Override
    public void sendEmail(String to, String subject, String content) {
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

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
            System.out.println("Email sent successfully to: " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Failed to send email to " + to);
        }
    }

    @Override
    public void sendPriceDropNotification(Course course, double oldPrice, List<Subscriber> subscribers) {
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
            // Using a new thread to not block the UI
            new Thread(() -> sendEmail(sub.getEmail(), subject, htmlContent)).start();
        }
    }
}
