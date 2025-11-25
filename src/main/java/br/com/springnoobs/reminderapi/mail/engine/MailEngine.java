package br.com.springnoobs.reminderapi.mail.engine;

import br.com.springnoobs.reminderapi.user.entity.Contact;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailEngine {

    Logger logger = org.slf4j.LoggerFactory.getLogger(MailEngine.class);

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Sao_Paulo"));

    public MailEngine(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(Contact contact, Reminder reminder) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(contact.getEmail());
            helper.setSubject("Lembrete" + " - " + reminder.getTitle());

            String mailTemplate = getEmailTemplateContent();

            Map<String, String> variables = buildEmailVariables(contact, reminder, "#");

            mailTemplate = replaceVariables(mailTemplate, variables);

            helper.setText(mailTemplate, true);

            mailSender.send(message);
        } catch (IOException | MessagingException e) {
            logger.error("Error at send email: {}", e.getMessage());
        }
    }

    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }

        return result;
    }

    private String getEmailTemplateContent() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/email/email-template.html");

        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private Map<String, String> buildEmailVariables(Contact contact, Reminder reminder, String disableUrl) {
        Map<String, String> map = new HashMap<>();

        map.put("name", contact.getUser().getFirstName());
        map.put("title", reminder.getTitle());
        map.put("remind_at", DATE_FORMATTER.format(reminder.getRemindAt()));
        map.put("disable_notification_url", disableUrl);

        return map;
    }
}
