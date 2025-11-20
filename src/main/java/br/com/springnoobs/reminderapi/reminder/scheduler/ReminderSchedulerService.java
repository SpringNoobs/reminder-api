package br.com.springnoobs.reminderapi.reminder.scheduler;

import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.repository.ReminderRepository;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class ReminderSchedulerService {

    private final ReminderRepository repository;
    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public ReminderSchedulerService(ReminderRepository repository, TaskScheduler taskScheduler) {
        this.repository = repository;
        this.taskScheduler = taskScheduler;
    }

    public void createSchedule(Reminder reminder) {
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> {
                    reminder.setExecutedAt(Instant.now());
                    reminder.setSent(true);
                    repository.save(reminder);
                    deleteReminderSchedule(reminder.getId());
                },
                reminder.getRemindAt());

        scheduledTasks.put(reminder.getId(), future);
    }

    public void updateSchedule(Reminder reminder) {
        ScheduledFuture<?> schedule = scheduledTasks.get(reminder.getId());

        if (schedule == null) {
            throw new NotFoundException("Schedule not found");
        }

        if (schedule.isDone() || reminder.isSent()) {
            throw new IllegalArgumentException("Not possible to update schedule");
        }

        schedule.cancel(false);

        deleteReminderSchedule(reminder.getId());

        createSchedule(reminder);
    }

    public void deleteSchedule(Reminder reminder) {
        ScheduledFuture<?> schedule = scheduledTasks.get(reminder.getId());

        if (schedule == null) {
            throw new NotFoundException("Schedule not found");
        }

        if (schedule.isDone() || reminder.isSent()) {
            throw new IllegalArgumentException("Not possible to delete schedule");
        }

        schedule.cancel(false);

        deleteReminderSchedule(reminder.getId());
    }

    public void deleteReminderSchedule(Long id) {
        if (!scheduledTasks.containsKey(id)) {
            return;
        }

        scheduledTasks.remove(id);
    }
}
