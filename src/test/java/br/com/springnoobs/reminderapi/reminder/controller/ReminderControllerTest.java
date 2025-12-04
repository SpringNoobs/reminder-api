package br.com.springnoobs.reminderapi.reminder.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.PastDueDateException;
import br.com.springnoobs.reminderapi.reminder.service.ReminderService;
import br.com.springnoobs.reminderapi.user.dto.request.ContactRequestDTO;
import br.com.springnoobs.reminderapi.user.dto.request.CreateUserRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReminderController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReminderService service;

    @Test
    void shouldReturnReminderListWhenListAllReminders() throws Exception {
        ReminderResponseDTO response1 = new ReminderResponseDTO("Lembrete A", Instant.now());
        ReminderResponseDTO response2 = new ReminderResponseDTO("Lembrete B", Instant.now());

        Page<ReminderResponseDTO> pageResult = new PageImpl<>(List.of(response1, response2));

        when(service.findAll(any())).thenReturn(pageResult);

        mockMvc.perform(get("/reminders?page=0&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Lembrete A"))
                .andExpect(jsonPath("$.content[1].title").value("Lembrete B"))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andDo(document("list-all-reminders"));
    }

    @Test
    public void shouldReturnEmptyReminderListWhenListAllReminders() throws Exception {
        when(service.findAll(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/reminders?page=0&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andDo(document("list-all-reminders-empty"));
    }

    @Test
    void shouldReturnReminderWhenFindByIdWithValidId() throws Exception {
        long reminderId = 1L;
        var response = new ReminderResponseDTO("Test Reminder", Instant.now());
        when(service.findById(reminderId)).thenReturn(response);

        mockMvc.perform(get("/reminders/{id}", reminderId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Reminder"))
                .andDo(document("find-reminder-by-id"));
    }

    @Test
    void shouldReturnNotFoundWhenFindByIdWithInvalidId() throws Exception {
        long reminderId = 99L;
        when(service.findById(reminderId)).thenThrow(new NotFoundException("Reminder not found"));

        mockMvc.perform(get("/reminders/{id}", reminderId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(document("find-reminder-by-id-not-found"));
    }

    @Test
    void shouldCreateReminderWhenRequestIsValid() throws Exception {
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO(
                "First Name", "Last Name", new ContactRequestDTO("email@test.com", "123456789"));

        var request = new CreateReminderRequestDTO("New Reminder", Instant.now().plusSeconds(60), createUserRequestDTO);
        var response = new ReminderResponseDTO(request.title(), request.dueDate());

        when(service.create(request)).thenReturn(response);

        mockMvc.perform(post("/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Reminder"))
                .andDo(document("create-reminder"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingReminderWithPastDate() throws Exception {
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO(
                "First Name", "Last Name", new ContactRequestDTO("email@test.com", "123456789"));

        var request =
                new CreateReminderRequestDTO("Past Reminder", Instant.now().minusSeconds(60), createUserRequestDTO);

        when(service.create(request)).thenThrow(new PastDueDateException("Remind at date must be in the future"));

        mockMvc.perform(post("/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-reminder-past-date"));
    }

    @Test
    void shouldUpdateReminderWhenRequestIsValid() throws Exception {
        long reminderId = 1L;
        var request =
                new UpdateReminderRequestDTO("Updated Reminder", Instant.now().plusSeconds(60));
        var response = new ReminderResponseDTO(request.title(), request.dueDate());

        when(service.update(reminderId, request)).thenReturn(response);

        mockMvc.perform(put("/reminders/{id}", reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Reminder"))
                .andDo(document("update-reminder"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingReminderWithPastDate() throws Exception {
        long reminderId = 1L;
        var request =
                new UpdateReminderRequestDTO("Past Reminder", Instant.now().minusSeconds(60));

        when(service.update(reminderId, request))
                .thenThrow(new PastDueDateException("Remind at date must be in the future"));

        mockMvc.perform(put("/reminders/{id}", reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-reminder-past-date"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentReminder() throws Exception {
        long reminderId = 99L;
        var request =
                new UpdateReminderRequestDTO("Updated Reminder", Instant.now().plusSeconds(60));

        when(service.update(reminderId, request)).thenThrow(new NotFoundException("Reminder not found"));

        mockMvc.perform(put("/reminders/{id}", reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("update-reminder-not-found"));
    }

    @Test
    void shouldDeleteReminderWhenIdIsValid() throws Exception {
        long reminderId = 1L;

        mockMvc.perform(delete("/reminders/{id}", reminderId))
                .andExpect(status().isNoContent())
                .andDo(document("delete-reminder"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentReminder() throws Exception {
        long reminderId = 99L;
        doThrow(new NotFoundException("Reminder not found")).when(service).delete(reminderId);

        mockMvc.perform(delete("/reminders/{id}", reminderId))
                .andExpect(status().isNotFound())
                .andDo(document("delete-reminder-not-found"));
    }
}
