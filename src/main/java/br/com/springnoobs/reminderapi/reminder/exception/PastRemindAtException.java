package br.com.springnoobs.reminderapi.reminder.exception;

public class PastRemindAtException extends RuntimeException {
    public PastRemindAtException(String message) {
        super(message);
    }
}
