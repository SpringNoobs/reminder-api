package br.com.springnoobs.reminderapi.mail.engine;

import br.com.springnoobs.reminderapi.mail.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    public MailEngine(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public MimeMessage createEmailMessage(Map<String, String> variables) {
        try
        {
            MimeMessage message = mailSender.createMimeMessage();


            // TODO: Teste de formatação de código
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(variables.get("email"));
            helper.setSubject(variables.get("subject"));

            String mailTemplate = getEmailTemplateContent();

            mailTemplate = replaceVariables(mailTemplate, variables);

            helper.setText(mailTemplate, true);

            return message;
        } catch (IOException | MessagingException e) {
            logger.error("Error at create email: {}", e.getMessage());
        }

        return null;
    }

    public void sendEmail(MimeMessage message) throws EmailSendException {
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException(e.getMessage());
        }
    }

    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String emailVariable = "{{" + entry.getKey() + "}}";

            if (result.contains(emailVariable)) {
                result = result.replace(emailVariable, entry.getValue());
            }
        }

        return result;
    }

    private String getEmailTemplateContent() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/email/email-template.html");

        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
