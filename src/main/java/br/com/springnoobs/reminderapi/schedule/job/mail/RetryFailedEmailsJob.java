package br.com.springnoobs.reminderapi.schedule.job.mail;

import br.com.springnoobs.reminderapi.mail.entity.EmailSendFailure;
import br.com.springnoobs.reminderapi.mail.exception.EmailSendException;
import br.com.springnoobs.reminderapi.mail.repository.EmailSendFailureRepository;
import br.com.springnoobs.reminderapi.mail.service.EmailService;
import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RetryFailedEmailsJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(RetryFailedEmailsJob.class);

    private final EmailSendFailureRepository repository;
    private final EmailService emailService;

    public RetryFailedEmailsJob(EmailSendFailureRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        List<EmailSendFailure> failureList = repository.findTop20ByOrderByFailedAtAsc();

        if (failureList.isEmpty()) {
            return;
        }

        for (EmailSendFailure failure : failureList) {
            retryFailedEmailsSent(failure);
        }
    }

    private void retryFailedEmailsSent(EmailSendFailure failure) {
        try {
            emailService.retryEmailSendFailure(failure);

            repository.delete(failure);

            logger.info("Email resend successful to {}", failure.getEmail());

        } catch (EmailSendException e) {

            logger.error("Failed to resend email ID {}: {}", failure.getId(), e.getMessage());

            failure.setRetryCount(failure.getRetryCount() + 1);
            failure.setErrorMessage(e.getMessage());
            repository.save(failure);
        }
    }
}
