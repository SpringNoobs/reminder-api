package br.com.springnoobs.reminderapi.reminder.controller;

import br.com.springnoobs.reminderapi.reminder.exception.NotFoundException;
import br.com.springnoobs.reminderapi.reminder.exception.PastDueDateException;
import br.com.springnoobs.reminderapi.reminder.exception.ReminderSchedulerException;
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

    private final Logger logger = LoggerFactory.getLogger(ReminderExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(ServletWebRequest request, NotFoundException ex) {
        logger.warn("Reminder not found {}", request.getRequest().getRequestURI(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PastDueDateException.class)
    public ProblemDetail handlePastDueDateException(ServletWebRequest request, PastDueDateException ex) {
        logger.warn("Past due date {}", request.getRequest().getRequestURI(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ReminderSchedulerException.class)
    public ProblemDetail handleReminderSchedulerException(ServletWebRequest request, ReminderSchedulerException ex) {
        logger.warn("Scheduler Exception {}", request.getRequest().getRequestURI(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
