package br.com.springnoobs.reminderapi.mail.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "email_send_failures")
public class EmailSendFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String subject;

    private String name;

    private String title;

    private String remindAt;

    private String dueDate;

    private String disableNotificationUrl;

    private Instant failedAt;

    private int retryCount;

    private String errorMessage;

    public EmailSendFailure() {}

    public EmailSendFailure(
            Long id,
            String email,
            String subject,
            String name,
            String title,
            String remindAt,
            String dueDate,
            String disableNotificationUrl,
            Instant failedAt,
            int retryCount,
            String errorMessage) {
        this.id = id;
        this.email = email;
        this.subject = subject;
        this.name = name;
        this.title = title;
        this.remindAt = remindAt;
        this.dueDate = dueDate;
        this.disableNotificationUrl = disableNotificationUrl;
        this.failedAt = failedAt;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(String remindAt) {
        this.remindAt = remindAt;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDisableNotificationUrl() {
        return disableNotificationUrl;
    }

    public void setDisableNotificationUrl(String disableNotificationUrl) {
        this.disableNotificationUrl = disableNotificationUrl;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
