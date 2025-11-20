package br.com.springnoobs.reminderapi.reminder.service;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.CreateReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.FindAllReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.FindReminderByIdResponseDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.UpdateReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.PastRemindAtException;
import br.com.springnoobs.reminderapi.reminder.mapper.ReminderMapper;
import br.com.springnoobs.reminderapi.reminder.repository.ReminderRepository;
import br.com.springnoobs.reminderapi.reminder.scheduler.ReminderSchedulerService;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

    private final ReminderRepository repository;
    private final ReminderSchedulerService reminderSchedulerService;

    public ReminderService(ReminderRepository repository, ReminderSchedulerService reminderSchedulerService) {
        this.repository = repository;
        this.reminderSchedulerService = reminderSchedulerService;
    }

    public CreateReminderResponseDTO create(CreateReminderRequestDTO dto) {
        if (dto.remindAt().isBefore(Instant.now())) {
            throw new PastRemindAtException("RemindAt should be a date in the future!");
        }

        Reminder reminder = new Reminder();
        BeanUtils.copyProperties(dto, reminder);

        Reminder savedReminder = repository.save(reminder);
        reminderSchedulerService.createSchedule(savedReminder);

        return ReminderMapper.toCreateReminderResponseDTO(savedReminder);
    }

    public FindReminderByIdResponseDTO findById(Long id) {
        var reminder = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found"));

        return ReminderMapper.toFindByIdResponseDTO(reminder);
    }

    public List<FindAllReminderResponseDTO> findAll(Pageable pageable) {
        return repository.findAllOrderByRemindAtAsc(pageable).stream()
                .map(ReminderMapper::toFindAllReminderResponseDTO)
                .toList();
    }

    public UpdateReminderResponseDTO update(Long id, UpdateReminderRequestDTO dto) {
        var reminder = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found"));

        if (dto.remindAt().isBefore(Instant.now())) {
            throw new PastRemindAtException("RemindAt should be a date in the future!");
        }

        reminderSchedulerService.updateSchedule(reminder);

        BeanUtils.copyProperties(dto, reminder);

        reminderSchedulerService.deleteSchedule(reminder);

        return ReminderMapper.toUpdateReminderResponseDTO(repository.save(reminder));
    }

    public void delete(Long id) {
        var reminder = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found"));

        repository.deleteById(id);

        reminderSchedulerService.deleteSchedule(reminder);
    }
}
