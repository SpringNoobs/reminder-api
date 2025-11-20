package br.com.springnoobs.reminderapi.reminder.bootstrap;

import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.reminder.repository.ReminderRepository;
import br.com.springnoobs.reminderapi.reminder.scheduler.ReminderSchedulerService;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReminderBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(ReminderBootstrap.class);

    private final ReminderRepository reminderRepository;
    private final ReminderSchedulerService schedulerService;

    public ReminderBootstrap(ReminderRepository reminderRepository, ReminderSchedulerService schedulerService) {
        this.reminderRepository = reminderRepository;
        this.schedulerService = schedulerService;
    }

    @PostConstruct
    public void restoreSchedules() {
        List<Reminder> pendingReminders =
                reminderRepository.findBySentFalseAndExecutedAtIsNullAndRemindAtAfter(Instant.now());

        for (Reminder reminder : pendingReminders) {
            schedulerService.createSchedule(reminder);
        }

        logger.debug("{} Pending reminders restored.", pendingReminders.size());
    }
}
