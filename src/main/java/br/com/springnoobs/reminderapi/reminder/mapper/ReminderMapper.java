package br.com.springnoobs.reminderapi.reminder.mapper;

import br.com.springnoobs.reminderapi.reminder.dto.response.CreateReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.FindAllReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.FindReminderByIdResponseDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.UpdateReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;

public class ReminderMapper {
    public static CreateReminderResponseDTO toCreateReminderResponseDTO(Reminder reminder) {
        return new CreateReminderResponseDTO(reminder.getTitle(), reminder.getRemindAt());
    }

    public static FindReminderByIdResponseDTO toFindByIdResponseDTO(Reminder reminder) {
        return new FindReminderByIdResponseDTO(reminder.getTitle(), reminder.getRemindAt());
    }

    public static FindAllReminderResponseDTO toFindAllReminderResponseDTO(Reminder reminder) {
        return new FindAllReminderResponseDTO(reminder.getTitle(), reminder.getRemindAt());
    }

    public static UpdateReminderResponseDTO toUpdateReminderResponseDTO(Reminder reminder) {
        return new UpdateReminderResponseDTO(reminder.getTitle(), reminder.getRemindAt());
    }
}
