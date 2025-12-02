package br.com.springnoobs.reminderapi.mail.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;

import br.com.springnoobs.reminderapi.mail.engine.MailEngine;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.user.entity.Contact;
import br.com.springnoobs.reminderapi.user.entity.User;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class EmailServiceTest {

    @Mock
    private MailEngine mailEngine;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSendEmailWhenReminderHasDueDate() {
        // Arrange
        User user = new User();
        user.setFirstName("John");
        Contact contact = new Contact();
        contact.setEmail("john.doe@test.com");

        user.setContact(contact);
        contact.setUser(user);

        Reminder reminder = new Reminder();
        reminder.setTitle("Test Reminder");
        reminder.setDueDate(Instant.now());
        reminder.setUser(user);

        // Act
        emailService.send(reminder);

        // Assert
        verify(mailEngine).sendEmail(anyMap());
    }

    @Test
    void shouldSendEmailWhenReminderHasRemindAtDate() {
        // Arrange
        User user = new User();
        user.setFirstName("John");
        Contact contact = new Contact();
        contact.setEmail("john.doe@test.com");

        user.setContact(contact);
        contact.setUser(user);

        Reminder reminder = new Reminder();
        reminder.setTitle("Test Reminder");
        reminder.setRemindAt(Instant.now());
        reminder.setDueDate(Instant.now());
        reminder.setUser(user);

        // Act
        emailService.send(reminder);

        // Assert
        verify(mailEngine).sendEmail(anyMap());
    }

    @Test
    void shouldThrowExceptionWhenReminderHasNoUser() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setTitle("Test Reminder");
        reminder.setDueDate(Instant.now());
        reminder.setUser(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> emailService.send(reminder));
    }
}
