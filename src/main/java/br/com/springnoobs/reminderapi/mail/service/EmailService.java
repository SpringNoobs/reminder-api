package br.com.springnoobs.reminderapi.mail.service;

import br.com.springnoobs.reminderapi.mail.engine.MailEngine;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.user.entity.Contact;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final MailEngine mailEngine;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Sao_Paulo"));

    public EmailService(MailEngine mailEngine) {
        this.mailEngine = mailEngine;
    }

    public void send(Reminder reminder) {
        if(reminder.getUser() == null)
        {
            throw new RuntimeException("User not found");
        }

        Contact contact = reminder.getUser().getContact();

        Map<String,String> variables = buildEmailVariables(contact, reminder, "#");

        mailEngine.sendEmail(variables);
    }

    private Map<String, String> buildEmailVariables(Contact contact, Reminder reminder, String disableUrl) {
        Map<String, String> map = new HashMap<>();

        map.put("name", contact.getUser().getFirstName());
        map.put("email", contact.getEmail());
        map.put("title", reminder.getTitle());
        map.put("remind_at", reminder.getRemindAt() != null ? DATE_FORMATTER.format(reminder.getRemindAt()) : DATE_FORMATTER.format(reminder.getDueDate()));
        map.put("due_date", DATE_FORMATTER.format(reminder.getDueDate()));
        map.put("disable_notification_url", disableUrl);
        map.put("subject", "Lembrete" + " - " + reminder.getTitle());

        return map;
    }
}
