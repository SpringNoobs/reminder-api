package br.com.springnoobs.reminderapi.reminder.controller;

import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.service.ReminderService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReminderService service;

    @Test
    void shouldReturnReminderListWhenListAllReminders() throws Exception {
        ReminderResponseDTO response1 = new ReminderResponseDTO("Lembrete A", Instant.now());
        ReminderResponseDTO response2 = new ReminderResponseDTO("Lembrete B", Instant.now());

        Page<ReminderResponseDTO> pageResult = new PageImpl<>(List.of(response1, response2));

        when(service.findAll(any())).thenReturn(pageResult);

        mockMvc.perform(get("/reminders?page=0&size=10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Lembrete A"))
                .andExpect(jsonPath("$.content[1].title").value("Lembrete B"))
                .andExpect(jsonPath("$.page.size").value(2));
    }

    @Test
    public void shouldReturnEmptyReminderListWhenListAllReminders() throws Exception {
        when(service.findAll(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/reminders?page=0&size=10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
