package br.com.springnoobs.reminderapi.reminder.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.ReminderSchedulerException;
import br.com.springnoobs.reminderapi.reminder.repository.ReminderRepository;
import br.com.springnoobs.reminderapi.schedule.service.JobService;
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
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class ReminderServiceTest {

    @Mock
    private ReminderRepository repository;

    @Mock
    private JobService jobService;

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
    void shouldCreateReminderWhenRequestIsValid() throws SchedulerException {
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO(
                "First Name", "Last Name", new ContactRequestDTO("email@test.com", "123456789"));

        // Arrange
        Instant dueDate = Instant.now().plusSeconds(60);
        CreateReminderRequestDTO request = new CreateReminderRequestDTO("Create", dueDate, createUserRequestDTO);

        Reminder reminder = new Reminder();
        reminder.setTitle(request.title());
        reminder.setDueDate(request.dueDate());

        User user = new User();

        user.setFirstName(createUserRequestDTO.firstName());
        user.setLastName(createUserRequestDTO.lastName());

        Contact contact = new Contact();
        contact.setEmail(createUserRequestDTO.contactRequestDTO().email());
        contact.setPhoneNumber(createUserRequestDTO.contactRequestDTO().phoneNumber());

        user.setContact(contact);

        reminder.setUser(user);

        when(repository.save(any())).thenReturn(reminder);
        when(userService.createAndSaveUser(createUserRequestDTO)).thenReturn(user);
        doNothing().when(jobService).scheduleJob(any(Reminder.class));

        // Act
        ReminderResponseDTO response = service.create(request);

        // Assert
        assertEquals("Create", response.title());
        assertEquals(dueDate, response.dueDate());
        verify(repository).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCreateReminderWithNonExistentUser() {
        // Arrange
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO(
                "First Name", "Last Name", new ContactRequestDTO("email@test.com", "123456789"));

        Instant dueDate = Instant.now().plusSeconds(60);
        CreateReminderRequestDTO request = new CreateReminderRequestDTO("Create", dueDate, createUserRequestDTO);

        when(userService.createAndSaveUser(any(CreateUserRequestDTO.class)))
                .thenThrow(new NotFoundException("User not found"));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> service.create(request));
    }

    @Test
    void shouldThrowReminderSchedulerExceptionWhenCreateReminderFailsToSchedule() throws SchedulerException {
        // Arrange
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO(
                "First Name", "Last Name", new ContactRequestDTO("email@test.com", "123456789"));

        Instant dueDate = Instant.now().plusSeconds(60);
        CreateReminderRequestDTO request = new CreateReminderRequestDTO("Create", dueDate, createUserRequestDTO);

        User user = new User();
        user.setFirstName("First Name");

        Reminder reminder = new Reminder();
        reminder.setTitle("Create");
        reminder.setDueDate(dueDate);
        reminder.setUser(user);

        when(userService.createAndSaveUser(any(CreateUserRequestDTO.class))).thenReturn(user);
        when(repository.save(any(Reminder.class))).thenReturn(reminder);
        doThrow(new SchedulerException("Failed to schedule job"))
                .when(jobService)
                .scheduleJob(any(Reminder.class));

        // Act & Assert
        assertThrows(ReminderSchedulerException.class, () -> service.create(request));
    }

    @Test
    void shouldUpdateReminderWhenRequestIsValid() throws SchedulerException {
        // Arrange
        Instant dueDate = Instant.now().plusSeconds(60);
        UpdateReminderRequestDTO request = new UpdateReminderRequestDTO("Update", dueDate);

        Reminder reminder = new Reminder();
        reminder.setTitle("Old Title");

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        when(repository.save(any())).thenReturn(reminder);
        doNothing().when(jobService).updateReminderSchedules(any(Reminder.class));

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
    void shouldThrowReminderSchedulerExceptionWhenUpdateReminderFailsToSchedule() throws SchedulerException {
        // Arrange
        Instant dueDate = Instant.now().plusSeconds(60);
        UpdateReminderRequestDTO request = new UpdateReminderRequestDTO("Update", dueDate);

        Reminder reminder = new Reminder();
        reminder.setTitle("Old Title");

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        doThrow(new SchedulerException("Failed to update job"))
                .when(jobService)
                .updateReminderSchedules(any(Reminder.class));

        // Act & Assert
        assertThrows(ReminderSchedulerException.class, () -> service.update(1L, request));
    }

    @Test
    void shouldDeleteReminderWhenReminderIdIsValid() throws SchedulerException {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setTitle("Delete");

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        doNothing().when(jobService).deleteReminderSchedules(1L);

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

    @Test
    void shouldThrowReminderSchedulerExceptionWhenDeleteReminderFailsToSchedule() throws SchedulerException {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setTitle("Delete");

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        doThrow(new SchedulerException("Failed to delete job")).when(jobService).deleteReminderSchedules(1L);

        // Act & Assert
        assertThrows(ReminderSchedulerException.class, () -> service.delete(1L));
    }

    @Test
    void shouldRegisterCompleteExecutionWhenReminderIsValid() {
        // Arrange
        Reminder reminder = new Reminder();
        reminder.setTitle("Any Title");

        when(repository.save(reminder)).thenReturn(reminder);

        // Act
        service.registerReminderExecution(reminder);

        // Assert
        assertTrue(reminder.isSent());
        assertNotNull(reminder.getExecutedAt());
        verify(repository).save(reminder);
    }

    @Test
    void shouldDisableReminderNotificationsWhenReminderIdIsValid() throws SchedulerException {
        // Arrange
        long reminderId = 1L;
        Reminder reminder = new Reminder();
        reminder.setId(reminderId);

        when(repository.findById(reminderId)).thenReturn(Optional.of(reminder));
        doNothing().when(jobService).unscheduleReminderJobTriggers(reminderId);

        // Act
        service.disableReminderNotifications(reminderId);

        // Assert
        verify(repository).findById(reminderId);
        verify(jobService).unscheduleReminderJobTriggers(reminderId);
        verifyNoMoreInteractions(repository, jobService);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDisablingNotificationsForInvalidReminderId() {
        // Arrange
        long invalidReminderId = 99L;
        when(repository.findById(invalidReminderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> service.disableReminderNotifications(invalidReminderId));

        // Verify
        verify(repository).findById(invalidReminderId);
        verifyNoInteractions(jobService);
    }
}
