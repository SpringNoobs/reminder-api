package br.com.springnoobs.reminderapi.mail.exception;

public class EmailSendException extends RuntimeException {

    public EmailSendException(String message) {
        super(message);
    }
}
