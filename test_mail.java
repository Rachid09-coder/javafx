import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class test_mail {
    public static void main(String[] args) {
        String SMTP_HOST = "smtp.gmail.com";
        String SMTP_PORT = "587";
        String SMTP_USERNAME = "logicien7@gmail.com";
        String SMTP_PASSWORD = "eibc admo lqbx lijf"; 

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME, "Boutique EduSmart"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("logicien7@gmail.com"));
            message.setSubject("Test Email");
            message.setContent("Test", "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
