package br.com.springnoobs.reminderapi.mail.service;

import br.com.springnoobs.reminderapi.mail.engine.MailEngine;
import br.com.springnoobs.reminderapi.mail.entity.EmailSendFailure;
import br.com.springnoobs.reminderapi.mail.exception.EmailSendException;
import br.com.springnoobs.reminderapi.mail.repository.EmailSendFailureRepository;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.user.entity.Contact;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    Logger logger = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    private final MailEngine mailEngine;

    private final EmailSendFailureRepository emailSendFailureRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Sao_Paulo"));

    public EmailService(MailEngine mailEngine, EmailSendFailureRepository emailSendFailureRepository) {
        this.mailEngine = mailEngine;
        this.emailSendFailureRepository = emailSendFailureRepository;
    }

    public void send(Reminder reminder) {
        if (reminder.getUser() == null) {
            throw new RuntimeException("User not found");
        }

        Contact contact = reminder.getUser().getContact();

        Map<String, String> variables = buildEmailVariables(contact, reminder, "#");

        MimeMessage mimeMessage = mailEngine.createEmailMessage(variables);

        if (mimeMessage == null) {
            return;
        }

        sendEmailWithFailureHandling(reminder, mimeMessage, variables);
    }

    void dispatchEmail(MimeMessage mimeMessage) throws EmailSendException {
        mailEngine.sendEmail(mimeMessage);
    }

    void sendEmailWithFailureHandling(Reminder reminder, MimeMessage mimeMessage, Map<String, String> variables) {
        try {
            dispatchEmail(mimeMessage);
        } catch (EmailSendException e) {
            logger.error("Error at send email: {}, to reminder:  {}", e.getMessage(), reminder.getId());

            registerEmailFailure(variables, e.getMessage());
        }
    }

    void registerEmailFailure(Map<String, String> variables, String errorMessage) {
        EmailSendFailure emailSendFailure = new EmailSendFailure();

        emailSendFailure.setName(variables.get("name"));
        emailSendFailure.setEmail(variables.get("email"));
        emailSendFailure.setTitle(variables.get("title"));
        emailSendFailure.setRemindAt(variables.get("remind_at"));
        emailSendFailure.setDueDate(variables.get("due_date"));
        emailSendFailure.setDisableNotificationUrl(variables.get("disable_notification_url"));
        emailSendFailure.setSubject(variables.get("subject"));
        emailSendFailure.setErrorMessage(errorMessage);
        emailSendFailure.setFailedAt(Instant.now());

        emailSendFailureRepository.save(emailSendFailure);
    }

    private Map<String, String> buildEmailVariables(Contact contact, Reminder reminder, String disableUrl) {
        Map<String, String> map = new HashMap<>();

        map.put("name", contact.getUser().getFirstName());
        map.put("email", contact.getEmail());
        map.put("title", reminder.getTitle());
        map.put(
                "remind_at",
                reminder.getRemindAt() != null
                        ? DATE_FORMATTER.format(reminder.getRemindAt())
                        : DATE_FORMATTER.format(reminder.getDueDate()));
        map.put("due_date", DATE_FORMATTER.format(reminder.getDueDate()));
        map.put("disable_notification_url", disableUrl);
        map.put("subject", "Lembrete" + " - " + reminder.getTitle());

        return map;
    }

    public void retryEmailSendFailure(EmailSendFailure emailSendFailure) {

        Map<String, String> variables = buildEmailParametersFromEmailFailure(emailSendFailure);

        MimeMessage mimeMessage = mailEngine.createEmailMessage(variables);

        if (mimeMessage == null) {
            return;
        }

        dispatchEmail(mimeMessage);
    }

    private static Map<String, String> buildEmailParametersFromEmailFailure(EmailSendFailure emailSendFailure) {
        Map<String, String> variables = new HashMap<>();

        variables.put("name", emailSendFailure.getName());
        variables.put("email", emailSendFailure.getEmail());
        variables.put("title", emailSendFailure.getTitle());
        variables.put(
                "remind_at",
                emailSendFailure.getRemindAt() != null
                        ? emailSendFailure.getRemindAt()
                        : emailSendFailure.getDueDate());
        variables.put("due_date", emailSendFailure.getDueDate());
        variables.put("disable_notification_url", emailSendFailure.getDisableNotificationUrl());
        variables.put("subject", "Lembrete" + " - " + emailSendFailure.getTitle());

        return variables;
    }
}
