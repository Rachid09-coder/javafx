package com.edusmart.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.Properties;

/**
 * Utility class for sending emails with attachments.
 */
public class MailSender {

    // SMTP Configuration
    // Common Hosts:
    // - Gmail: smtp.gmail.com (Port 587)
    // - Outlook/Hotmail: smtp.office365.com (Port 587)
    // - Yahoo: smtp.mail.yahoo.com (Port 465 or 587)

    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = "rachidgharbi09@gmail.com";
    private static final String PASSWORD = "qzzx ssrv njuw wmat";

    public static void sendEmailWithAttachment(String to, String subject, String body, File attachment)
            throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        // Body part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Attachment part
        if (attachment != null && attachment.exists()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            try {
                attachmentPart.attachFile(attachment);
                multipart.addBodyPart(attachmentPart);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        message.setContent(multipart);
        Transport.send(message);
    }
}
