package br.com.springnoobs.reminderapi.schedule.job.reminder;

import br.com.springnoobs.reminderapi.mail.service.EmailService;
import br.com.springnoobs.reminderapi.reminder.repository.ReminderRepository;
import br.com.springnoobs.reminderapi.reminder.service.ReminderService;
import java.time.Instant;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class ReminderJob extends QuartzJobBean {

    Logger logger = org.slf4j.LoggerFactory.getLogger(ReminderJob.class);

    private final ReminderRepository reminderRepository;

    private final ReminderService reminderService;

    private final EmailService emailService;

    public ReminderJob(
            ReminderRepository reminderRepository, ReminderService reminderService, EmailService emailService) {
        this.reminderRepository = reminderRepository;
        this.reminderService = reminderService;
        this.emailService = emailService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        long reminderId = context.getMergedJobDataMap().getLong("reminder-id");

        reminderRepository.findByIdWithAssociations(reminderId).ifPresent(reminder -> {
            emailService.send(reminder);

            reminderService.registerReminderExecution(reminder);

            logger.info("Executed reminder {} at {}", reminder.getId(), Instant.now());
        });
    }
}
