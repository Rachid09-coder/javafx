package com.edusmart.service;

import com.edusmart.model.Course;
import com.edusmart.model.Subscriber;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.List;
import java.util.Properties;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
    void sendPriceDropNotification(Course course, double oldPrice, List<Subscriber> subscribers);

    static void sendOrderConfirmationEmail(String toEmail, String studentName, int orderId, String trackingNumber, File invoicePdf) throws Exception {
        String smtpUsername = System.getenv("SMTP_USERNAME");
        String smtpPassword = System.getenv("SMTP_PASSWORD");
        if (smtpUsername == null || smtpPassword == null) {
            throw new Exception("Variables d'environnement SMTP_USERNAME et SMTP_PASSWORD non définies.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpUsername, "Boutique EduSmart"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Confirmation de votre commande #" + orderId);

        String htmlContent = "<h2>Merci pour votre commande, " + studentName + " !</h2>"
                + "<p>Votre commande a été traitée avec succès. Elle est en cours de préparation.</p>"
                + "<p>📦 <b>Numéro de suivi de colis :</b> <span style='color: #10B981; font-weight: bold;'>" + trackingNumber + "</span></p>"
                + "<p>Vous trouverez la facture en pièce jointe.</p>"
                + "<p>Vous recevrez un autre e-mail lors de l'expédition.</p>"
                + "<p><br/>Cordialement,<br/>L'équipe EduSmart</p>";

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(htmlContent, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        if (invoicePdf != null && invoicePdf.exists()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(invoicePdf);
            multipart.addBodyPart(attachmentPart);
        }

        message.setContent(multipart);

        if (!smtpUsername.equals("votre.email@gmail.com")) {
            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + toEmail);
        } else {
            System.out.println("⚠️ Simulation d'envoi d'e-mail (Veuillez configurer SMTP_USERNAME) à " + toEmail);
            throw new Exception("L'adresse email d'envoi n'est pas configurée dans EmailService.");
        }
    }
}
