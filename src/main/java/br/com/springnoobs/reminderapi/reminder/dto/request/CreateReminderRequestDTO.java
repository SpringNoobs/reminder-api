package br.com.springnoobs.reminderapi.reminder.dto.request;

import br.com.springnoobs.reminderapi.user.dto.request.CreateUserRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateReminderRequestDTO(
        @NotBlank(message = "Title must not be null") String title,
        @NotNull(message = "DueDate must not be null") Instant dueDate,
        @NotNull(message = "User must not be null") CreateUserRequestDTO user) {}
