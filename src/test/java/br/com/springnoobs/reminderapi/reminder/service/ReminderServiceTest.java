package br.com.springnoobs.reminderapi.reminder.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.PastDueDateException;
import br.com.springnoobs.reminderapi.reminder.repository.ReminderRepository;
import br.com.springnoobs.reminderapi.reminder.scheduler.ReminderSchedulerService;
import br.com.springnoobs.reminderapi.user.dto.request.ContactRequestDTO;
import br.com.springnoobs.reminderapi.user.dto.request.CreateUserRequestDTO;
import br.com.springnoobs.reminderapi.user.entity.Contact;
import br.com.springnoobs.reminderapi.user.entity.User;
import br.com.springnoobs.reminderapi.user.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class ReminderServiceTest {

    @Mock
    private ReminderRepository repository;

    @Mock
    private ReminderSchedulerService reminderSchedulerService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReminderService service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnEmptyReminderPageWhenRepositoryIsEmpty() {
        // Arrange
        when(repository.findAllByOrderByRemindAtAsc(any(Pageable.class))).thenReturn(Page.empty());

        // Act
        Page<ReminderResponseDTO> reminderPage = service.findAll(Pageable.unpaged());

        // Assert
        assertNotNull(reminderPage);
        assertTrue(reminderPage.isEmpty());
    }

    @Test
    void shouldReturnReminderPageWhenRepositoryIsNotEmpty() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setTitle("Title");
        reminder.setRemindAt(Instant.now().plusSeconds(60));

        when(repository.findAllByOrderByRemindAtAsc(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(reminder)));

        // Act
        Page<ReminderResponseDTO> page = service.findAll(Pageable.unpaged());

        // Assert
        assertEquals(1, page.getTotalElements());
        assertEquals("Title", page.getContent().getFirst().title());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTryFindReminderByIdWithInvalidId() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act And Assert
        assertThrows(NotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void shouldReturnReminderWhenTryFindReminderByIdWithValidId() {
        // Arrange
        Reminder mockReminder = new Reminder();
        mockReminder.setTitle("Title");

        when(repository.findById(1L)).thenReturn(Optional.of(mockReminder));

        // Act
        ReminderResponseDTO reminderDTO = service.findById(1L);

        //  Assert
        verify(repository).findById(1L);
        assertNotNull(reminderDTO);
        assertEquals("Title", reminderDTO.title());
    }

    @Test
    void shouldCreateReminderWhenRequestIsValid() {
        // Arrange
        Instant dueDate = Instant.now().plusSeconds(60);
        CreateReminderRequestDTO request = new CreateReminderRequestDTO("Create", dueDate);

        Reminder reminder = new Reminder();
        reminder.setTitle(request.title());
        reminder.setDueDate(request.dueDate());
        when(repository.save(any())).thenReturn(reminder);

        // Act
        ReminderResponseDTO response = service.create(request);

        // Assert
        assertEquals("Create", response.title());
        assertEquals(dueDate, response.dueDate());
        verify(repository).save(any());
    }

    @Test
    void shouldThrowDueDateExceptionWhenTryCreateReminderWithPastDueDate() {
        // Arrange
        Instant dueDate = Instant.now().minusSeconds(60);
        CreateReminderRequestDTO request = new CreateReminderRequestDTO("Create", dueDate);

        // Act And Assert
        assertThrows(PastDueDateException.class, () -> service.create(request));
    }

    @Test
    void shouldUpdateReminderWhenRequestIsValid() {
        // Arrange
        Instant dueDate = Instant.now().plusSeconds(60);
        UpdateReminderRequestDTO request = new UpdateReminderRequestDTO("Update", dueDate);

        Reminder reminder = new Reminder();
        reminder.setTitle("Old Title");

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        when(repository.save(any())).thenReturn(reminder);

        // Act
        ReminderResponseDTO response = service.update(1L, request);

        // Assert
        assertEquals("Update", response.title());
        assertEquals(dueDate, response.dueDate());
        verify(repository).findById(1L);
        verify(repository).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTryUpdateReminderWithInvalidId() {
        // Arrange
        Instant dueDate = Instant.now().plusSeconds(60);
        UpdateReminderRequestDTO request = new UpdateReminderRequestDTO("Update", dueDate);

        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act And Assert
        assertThrows(NotFoundException.class, () -> service.update(1L, request));
    }

    @Test
    void shouldThrowPastDueDateExceptionWhenTryUpdateReminderWithPastDueDate() {
        // Arrange
        Instant remindAt = Instant.now().minusSeconds(60);
        UpdateReminderRequestDTO request = new UpdateReminderRequestDTO("Update", remindAt);

        Reminder reminder = new Reminder();
        reminder.setTitle("Any Title");
        reminder.setDueDate(request.dueDate());

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));

        // Act And Assert
        assertThrows(PastDueDateException.class, () -> service.update(1L, request));
        verify(repository).findById(1L);
    }

    @Test
    void shouldDeleteReminderWhenReminderIdIsValid() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setTitle("Delete");

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));

        // Act
        service.delete(1L);

        // Assert
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTryDeleteReminderWithInvalidId() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act And Assert
        assertThrows(NotFoundException.class, () -> service.delete(1L));
    }
}
