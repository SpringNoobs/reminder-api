package br.com.springnoobs.reminderapi.reminder.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.PastRemindAtException;
import br.com.springnoobs.reminderapi.reminder.service.ReminderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReminderController.class)
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
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    public void shouldReturnEmptyReminderListWhenListAllReminders() throws Exception {
        when(service.findAll(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/reminders?page=0&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldReturnReminderWhenFindByIdWithValidId() throws Exception {
        long reminderId = 1L;
        var response = new ReminderResponseDTO("Test Reminder", Instant.now());
        when(service.findById(reminderId)).thenReturn(response);

        mockMvc.perform(get("/reminders/{id}", reminderId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Reminder"));
    }

    @Test
    void shouldReturnNotFoundWhenFindByIdWithInvalidId() throws Exception {
        long reminderId = 99L;
        when(service.findById(reminderId)).thenThrow(new NotFoundException("Reminder not found"));

        mockMvc.perform(get("/reminders/{id}", reminderId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateReminderWhenRequestIsValid() throws Exception {
        var request = new CreateReminderRequestDTO("New Reminder", Instant.now().plusSeconds(60));
        var response = new ReminderResponseDTO("New Reminder", request.dueDate());

        when(service.create(any(CreateReminderRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Reminder"));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingReminderWithPastDate() throws Exception {
        var request =
                new CreateReminderRequestDTO("Past Reminder", Instant.now().minusSeconds(60));

        when(service.create(any(CreateReminderRequestDTO.class)))
                .thenThrow(new PastRemindAtException("Remind at date must be in the future"));

        mockMvc.perform(post("/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateReminderWhenRequestIsValid() throws Exception {
        long reminderId = 1L;
        var request =
                new UpdateReminderRequestDTO("Updated Reminder", Instant.now().plusSeconds(60));
        var response = new ReminderResponseDTO("Updated Reminder", request.dueDate());

        when(service.update(any(Long.class), any(UpdateReminderRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/reminders/{id}", reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Reminder"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingReminderWithPastDate() throws Exception {
        long reminderId = 1L;
        var request =
                new UpdateReminderRequestDTO("Past Reminder", Instant.now().minusSeconds(60));

        when(service.update(any(Long.class), any(UpdateReminderRequestDTO.class)))
                .thenThrow(new PastRemindAtException("Remind at date must be in the future"));

        mockMvc.perform(put("/reminders/{id}", reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentReminder() throws Exception {
        long reminderId = 99L;
        var request =
                new UpdateReminderRequestDTO("Updated Reminder", Instant.now().plusSeconds(60));

        when(service.update(any(Long.class), any(UpdateReminderRequestDTO.class)))
                .thenThrow(new NotFoundException("Reminder not found"));

        mockMvc.perform(put("/reminders/{id}", reminderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteReminderWhenIdIsValid() throws Exception {
        long reminderId = 1L;

        mockMvc.perform(delete("/reminders/{id}", reminderId)).andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentReminder() throws Exception {
        long reminderId = 99L;
        doThrow(new NotFoundException("Reminder not found")).when(service).delete(reminderId);

        mockMvc.perform(delete("/reminders/{id}", reminderId)).andExpect(status().isNotFound());
    }
}
