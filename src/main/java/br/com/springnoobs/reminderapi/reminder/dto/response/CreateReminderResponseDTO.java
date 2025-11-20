package br.com.springnoobs.reminderapi.reminder.dto.response;

import java.time.Instant;

public record CreateReminderResponseDTO(String title, Instant remindAt) {}
