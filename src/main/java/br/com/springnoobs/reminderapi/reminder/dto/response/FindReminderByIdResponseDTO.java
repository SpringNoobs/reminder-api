package br.com.springnoobs.reminderapi.reminder.dto.response;

import java.time.Instant;

public record FindReminderByIdResponseDTO(String title, Instant remindAt) {}
