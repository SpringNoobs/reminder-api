package br.com.springnoobs.reminderapi.reminder.dto.response;

import java.time.Instant;

public record ReminderResponseDTO(String title, Instant dueDate) {}
