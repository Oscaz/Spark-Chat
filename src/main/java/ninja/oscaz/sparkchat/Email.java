package ninja.oscaz.sparkchat;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Email {

    private Email() { throw new IllegalStateException("Cannot be instantiated!"); }

    // Utility method for sending a support email from and to our own gmail account.
    static void sendToSelf(String subject, String contents) {
        final String adress = "sparkchatist@gmail.com";
        final String password = "sparksupport";

        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(adress, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(adress));

            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(adress));

            message.setSubject(subject);

            message.setText(contents);

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
