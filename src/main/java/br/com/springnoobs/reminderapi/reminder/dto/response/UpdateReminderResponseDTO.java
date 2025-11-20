package br.com.springnoobs.reminderapi.reminder.dto.response;

import java.time.Instant;

public record UpdateReminderResponseDTO(String title, Instant remindAt) {}
