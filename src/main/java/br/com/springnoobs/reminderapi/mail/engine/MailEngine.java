package br.com.springnoobs.reminderapi.mail.engine;

import br.com.springnoobs.reminderapi.user.entity.Contact;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailEngine {

    Logger logger = org.slf4j.LoggerFactory.getLogger(MailEngine.class);

    private final JavaMailSender mailSender;

    public MailEngine(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(Contact contact, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(contact.getEmail());
            helper.setSubject(subject);

            String mailTemplate = prepareMailTemplate(getMailTemplate(), body);

            helper.setText(mailTemplate, true);

            mailSender.send(message);
        } catch (IOException | MessagingException e) {
            logger.error("Error at send email: {}", e.getMessage());
        }
    }

    private String prepareMailTemplate(String mailTemplate, String body) {
        return mailTemplate.replace("{{body}}", body);
    }

    public String getMailTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/email/email-template.html");

        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
