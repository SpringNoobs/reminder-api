package br.com.springnoobs.reminderapi.reminder.service;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.PastDueDateException;
import br.com.springnoobs.reminderapi.reminder.mapper.ReminderMapper;
import br.com.springnoobs.reminderapi.reminder.repository.ReminderRepository;
import br.com.springnoobs.reminderapi.reminder.scheduler.ReminderSchedulerService;
import java.time.Instant;

import br.com.springnoobs.reminderapi.user.entity.User;
import br.com.springnoobs.reminderapi.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderService {

    private final ReminderRepository repository;
    private final UserService userService;
    private final ReminderSchedulerService reminderSchedulerService;

    public ReminderService(ReminderRepository repository, UserService userService, ReminderSchedulerService reminderSchedulerService) {
        this.repository = repository;
        this.userService = userService;
        this.reminderSchedulerService = reminderSchedulerService;
    }

    @Transactional
    public ReminderResponseDTO create(CreateReminderRequestDTO dto) {
        if (dto.dueDate().isBefore(Instant.now())) {
            throw new PastDueDateException("DueDate should be a date in the future!");
        }

        Reminder reminder = new Reminder();
        BeanUtils.copyProperties(dto, reminder);

        User user = userService.createAndSaveUser(dto.user());

        reminder.setUser(user);

        Reminder savedReminder = repository.save(reminder);
        reminderSchedulerService.createSchedule(savedReminder);

        return ReminderMapper.toResponse(savedReminder);
    }

    public ReminderResponseDTO findById(Long id) {
        var reminder = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found"));

        return ReminderMapper.toResponse(reminder);
    }

    public Page<ReminderResponseDTO> findAll(Pageable pageable) {
        return repository.findAllByOrderByRemindAtAsc(pageable).map(ReminderMapper::toResponse);
    }

    public ReminderResponseDTO update(Long id, UpdateReminderRequestDTO dto) {
        var reminder = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found"));

        if (dto.dueDate().isBefore(Instant.now())) {
            throw new PastDueDateException("DueDate should be a date in the future!");
        }

        reminderSchedulerService.deleteSchedule(reminder);

        BeanUtils.copyProperties(dto, reminder);

        reminderSchedulerService.createSchedule(reminder);

        return ReminderMapper.toResponse(repository.save(reminder));
    }

    public void delete(Long id) {
        var reminder = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found"));

        reminderSchedulerService.deleteSchedule(reminder);

        repository.deleteById(id);
    }

    public void registerReminderExecution(Reminder reminder) {
        reminder.setExecutedAt(Instant.now());
        reminder.setSent(true);

        repository.save(reminder);
    }
}
