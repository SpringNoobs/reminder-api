package br.com.springnoobs.reminderapi.reminder.dto.response;

import java.time.Instant;

public record FindAllReminderResponseDTO(String title, Instant remindAt) {}
