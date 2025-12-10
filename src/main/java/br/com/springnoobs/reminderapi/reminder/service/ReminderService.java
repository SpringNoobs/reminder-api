package br.com.springnoobs.reminderapi.reminder.service;

import br.com.springnoobs.reminderapi.reminder.dto.request.CreateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.request.UpdateReminderRequestDTO;
import br.com.springnoobs.reminderapi.reminder.dto.response.ReminderResponseDTO;
import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.ReminderSchedulerException;
import br.com.springnoobs.reminderapi.reminder.mapper.ReminderMapper;
import br.com.springnoobs.reminderapi.reminder.repository.ReminderRepository;
import br.com.springnoobs.reminderapi.schedule.service.JobService;
import br.com.springnoobs.reminderapi.user.entity.User;
import br.com.springnoobs.reminderapi.user.service.UserService;
import java.time.Instant;
import java.util.Optional;

import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderService {

    private final ReminderRepository repository;
    private final UserService userService;
    private final JobService jobService;

    public ReminderService(ReminderRepository repository, UserService userService, JobService jobService) {
        this.repository = repository;
        this.userService = userService;
        this.jobService = jobService;
    }

    @Transactional
    public ReminderResponseDTO create(CreateReminderRequestDTO dto) {
        try {
            Reminder reminder = new Reminder();
            BeanUtils.copyProperties(dto, reminder);

            User user = userService.createAndSaveUser(dto.user());

            reminder.setUser(user);

            Reminder savedReminder = repository.save(reminder);
            jobService.scheduleJob(savedReminder);

            return ReminderMapper.toResponse(savedReminder);

        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (SchedulerException e) {
            throw new ReminderSchedulerException(e.getMessage());
        }
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
        try {
            var reminder = repository
                    .findById(id)
                    .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found"));

            BeanUtils.copyProperties(dto, reminder);

            jobService.updateReminderSchedules(reminder);

            return ReminderMapper.toResponse(repository.save(reminder));
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (SchedulerException e) {
            throw new ReminderSchedulerException(e.getMessage());
        }
    }

    public void delete(Long id) {
        try {
            var reminder = repository
                    .findById(id)
                    .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found"));

            jobService.deleteReminderSchedules(reminder.getId());

            repository.deleteById(id);
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (SchedulerException e) {
            throw new ReminderSchedulerException(e.getMessage());
        }
    }

    public void registerReminderExecution(Reminder reminder) {
        reminder.setExecutedAt(Instant.now());
        reminder.setSent(true);

        repository.save(reminder);
    }

    public ReminderResponseDTO disableEmail(Long id){

        var reminder = repository.
                findById(id)
                .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found")
        );

        reminder.setEmailEnabled(false);

        return ReminderMapper.toResponse(repository.save(reminder));
    }

    public ReminderResponseDTO enableEmail(Long id){

        var reminder = repository.
                findById(id)
                .orElseThrow(() -> new NotFoundException("Reminder with ID: " + id + " not found")
        );

        reminder.setEmailEnabled(true);
        return ReminderMapper.toResponse(repository.save(reminder));
    }

}
