package br.com.springnoobs.reminderapi.reminder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UpdateReminderRequestDTO(
        @NotBlank(message = "Title must not be null") String title,
        @NotNull(message = "DueDate must not be null") Instant dueDate) {}
