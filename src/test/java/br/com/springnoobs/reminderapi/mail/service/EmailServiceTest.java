package br.com.springnoobs.reminderapi.mail.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.springnoobs.reminderapi.mail.engine.MailEngine;
import br.com.springnoobs.reminderapi.mail.entity.EmailSendFailure;
import br.com.springnoobs.reminderapi.mail.exception.EmailSendException;
import br.com.springnoobs.reminderapi.mail.repository.EmailSendFailureRepository;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.user.entity.Contact;
import br.com.springnoobs.reminderapi.user.entity.User;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServiceTest {

    @Mock
    private MailEngine mailEngine;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailSendFailureRepository emailSendFailureRepository;

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

        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(mailEngine.createEmailMessage(any())).thenReturn(message);

        // Act
        emailService.send(reminder);

        // Assert
        verify(mailEngine).sendEmail(message);
    }

    @Test
    void shouldThrowEmailSendExceptionWhenTrySendEmailWithFailureHandling() {
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

        MimeMessage mimeMessage = mock(MimeMessage.class);
        Map<String, String> variables = new HashMap<>();

        EmailService service = Mockito.spy(new EmailService(mailEngine, emailSendFailureRepository));

        doThrow(new EmailSendException("SMTP Error")).when(mailEngine).sendEmail(mimeMessage);

        // Act
        service.sendEmailWithFailureHandling(reminder, mimeMessage, variables);

        // Assert
        Mockito.verify(service).registerEmailFailure(variables, "SMTP Error");
        verify(emailSendFailureRepository).save(any());
    }

    @Test
    void shouldSendEmailWhenRetryEmailSendFailureWithRemindAtDate() {
        // Arrange
        EmailSendFailure emailSendFailure = new EmailSendFailure();

        emailSendFailure.setName("Lucas");
        emailSendFailure.setEmail("test@example.com");
        emailSendFailure.setTitle("Test Reminder");
        emailSendFailure.setRemindAt("11/12/2025");
        emailSendFailure.setDueDate("11/12/2025");
        emailSendFailure.setDisableNotificationUrl("#");
        emailSendFailure.setSubject("Test - Subject");
        emailSendFailure.setErrorMessage("SMTP Error");

        MimeMessage message = mock(MimeMessage.class);
        when(mailEngine.createEmailMessage(any())).thenReturn(message);

        EmailService service = Mockito.spy(new EmailService(mailEngine, emailSendFailureRepository));

        // Act
        service.retryEmailSendFailure(emailSendFailure);

        // Assert
        verify(service).dispatchEmail(any());
    }

    @Test
    void shouldReturnWhenRetryEmailSendFailureWithNullMimeMessageAndWithoutRemindAtDate() {
        // Arrange
        EmailSendFailure emailSendFailure = new EmailSendFailure();

        emailSendFailure.setName("Lucas");
        emailSendFailure.setEmail("test@example.com");
        emailSendFailure.setTitle("Test Reminder");
        emailSendFailure.setDueDate("11/12/2025");
        emailSendFailure.setDisableNotificationUrl("#");
        emailSendFailure.setSubject("Test - Subject");
        emailSendFailure.setErrorMessage("SMTP Error");

        when(mailEngine.createEmailMessage(any())).thenReturn(null);

        EmailService service = Mockito.spy(new EmailService(mailEngine, emailSendFailureRepository));

        // Act
        service.retryEmailSendFailure(emailSendFailure);

        // Assert
        verify(service, never()).dispatchEmail(any());
    }

    @Test
    void shouldNotSendEmailWhenReminderHasRemindAtDateAndNotCreateMimeMessage() {
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

        when(mailEngine.createEmailMessage(any())).thenReturn(null);

        EmailService service = Mockito.spy(new EmailService(mailEngine, emailSendFailureRepository));

        // Act
        service.send(reminder);

        // Assert
        verify(service, never()).sendEmailWithFailureHandling(any(), any(), any());
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
