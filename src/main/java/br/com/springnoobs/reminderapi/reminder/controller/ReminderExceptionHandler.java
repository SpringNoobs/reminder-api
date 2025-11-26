package br.com.springnoobs.reminderapi.reminder.controller;

import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.PastRemindAtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ReminderExceptionHandler extends ResponseEntityExceptionHandler {

    // Exceptions criadas em br.com.springnoobs.reminderapi.reminder.exception;

    private final Logger logger = LoggerFactory.getLogger(ReminderExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(ServletWebRequest request, NotFoundException ex) {
        logger.warn("Reminder not found {}", request.getRequest().getRequestURI(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PastRemindAtException.class)
    public ProblemDetail handlePastRemindAtException(ServletWebRequest request, PastRemindAtException ex) {
        logger.warn("Past remindAt {}", request.getRequest().getRequestURI(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
