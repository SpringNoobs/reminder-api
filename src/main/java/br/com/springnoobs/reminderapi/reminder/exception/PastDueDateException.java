package br.com.springnoobs.reminderapi.reminder.exception;

public class PastDueDateException extends RuntimeException {
    public PastDueDateException(String message) {
        super(message);
    }
}
