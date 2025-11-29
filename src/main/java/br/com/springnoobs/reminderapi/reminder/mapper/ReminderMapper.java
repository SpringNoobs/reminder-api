package br.com.springnoobs.reminderapi.reminder.mapper;

import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;

public class ReminderMapper {
    public static ReminderResponseDTO toResponse(Reminder reminder) {
        return new ReminderResponseDTO(reminder.getTitle(), reminder.getDueDate());
    }
}
