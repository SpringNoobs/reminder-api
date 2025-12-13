package br.com.springnoobs.reminderapi.schedule.service;

import br.com.springnoobs.reminderapi.reminder.entity.Reminder;
import br.com.springnoobs.reminderapi.schedule.job.mail.RetryFailedEmailsJob;
import br.com.springnoobs.reminderapi.schedule.job.reminder.ReminderJob;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private static final String TRIGGER_NAME = "reminder-trigger";
    private static final String JOB_NAME = "reminder-job";
    private static final String JOB_GROUP = "reminders";

    private static final String RETRY_EMAIL_JOB_NAME = "retry-email-job";
    private static final String RETRY_EMAIL_TRIGGER_NAME = "retry-email-trigger";
    private static final String RETRY_EMAIL_GROUP = "email-retry";

    private final Scheduler scheduler;

    public JobService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    public void initializeRetryEmailJob() throws SchedulerException {
        scheduleRetryEmailJob();
    }

    public void scheduleJob(Reminder reminder) throws SchedulerException {

        JobDetail jobDetail = JobBuilder.newJob(ReminderJob.class)
                .withIdentity(JOB_NAME + "-" + reminder.getId(), JOB_GROUP)
                .usingJobData("reminder-id", reminder.getId())
                .storeDurably()
                .build();

        scheduler.addJob(jobDetail, true);

        scheduleDueDateTriggers(reminder);
    }

    public void updateReminderSchedules(Reminder reminder) throws SchedulerException {
        deleteReminderSchedules(reminder.getId());

        scheduleJob(reminder);
    }

    public void deleteReminderSchedules(Long reminderId) throws SchedulerException {
        JobKey jobKey = new JobKey(JOB_NAME + "-" + reminderId, JOB_GROUP);

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
    }

    private void scheduleDueDateTriggers(Reminder reminder) throws SchedulerException {
        scheduleTrigger(reminder, "10-days", reminder.getDueDate().minus(Duration.ofDays(10)));
        scheduleTrigger(reminder, "5-days", reminder.getDueDate().minus(Duration.ofDays(5)));
        scheduleTrigger(reminder, "2-days", reminder.getDueDate().minus(Duration.ofDays(2)));
    }

    private void scheduleTrigger(Reminder reminder, String suffix, Instant fireTime) throws SchedulerException {

        if (fireTime.isBefore(Instant.now())) {
            return;
        }

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(TRIGGER_NAME + "-" + reminder.getId() + "-" + suffix, JOB_GROUP)
                .forJob(JOB_NAME + "-" + reminder.getId(), JOB_GROUP)
                .startAt(Date.from(fireTime))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(trigger);
    }

    public void scheduleRetryEmailJob() throws SchedulerException {

        JobDetail jobDetail = JobBuilder.newJob(RetryFailedEmailsJob.class)
                .withIdentity(RETRY_EMAIL_JOB_NAME, RETRY_EMAIL_GROUP)
                .storeDurably()
                .build();

        scheduler.addJob(jobDetail, true);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(RETRY_EMAIL_TRIGGER_NAME, RETRY_EMAIL_GROUP)
                .forJob(jobDetail)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(20)
                        .repeatForever()
                        .withMisfireHandlingInstructionFireNow())
                .startNow()
                .build();

        scheduler.scheduleJob(trigger);
    }
}
