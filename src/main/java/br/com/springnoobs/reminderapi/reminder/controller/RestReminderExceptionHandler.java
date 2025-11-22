package br.com.springnoobs.reminderapi.reminder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import br.com.springnoobs.reminderapi.reminder.exception.*;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.*;

@RestControllerAdvice
public class RestReminderExceptionHandler {

    // Exceptions criadas em br.com.springnoobs.reminderapi.reminder.exception;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(PastRemindAtException.class)
    public ResponseEntity<ErrorResponse> handlePastRemindAtException(PastRemindAtException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

//    // Exception gerais do Spring
//    // Podem ocorrer em qualquer modulo da aplicação
//    // Talvez seria melhor criar um ExceptionHandlerGeral pra toda a aplicação, e incluir estes
//
//   // Validação de campos @Valid
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
//        ErrorResponse errorResponse = new ErrorResponse();
//
//        ex.getBindingResult().getFieldErrors().forEach(error ->
//                errorResponse.addFieldError(error.getField(), error.getDefaultMessage())
//        );
//
//        return ResponseEntity.badRequest().body(errorResponse);
//    }
//
//    // Erros de JSON mal formatado
//
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
//
//        ErrorResponse errorResponse = new ErrorResponse();
//
//        Throwable cause = ex.getCause();
//
//        // Caso 1: Body ausente
//        if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
//            errorResponse.addError("The request body is required and was not provided.");
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//
//        // Caso 2: Erros de parse Jackson
//        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormat) {
//            String fieldName = invalidFormat.getPath().stream()
//                    .map(ref -> ref.getFieldName())
//                    .filter(Objects::nonNull)
//                    .findFirst()
//                    .orElse("unknown field");
//
//            errorResponse.addError(
//                    String.format(
//                            "Invalid value for field '%s'. Expected: %s.",
//                            fieldName,
//                            invalidFormat.getTargetType().getSimpleName()
//                    )
//            );
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//
//        // Caso 3: Data mal formatada
//        if (cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException mismatched) {
//            errorResponse.addError("Invalid JSON format or incompatible types.");
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//
//        // Fallback (evita vazar stack trace interna)
//        errorResponse.addError("Invalid or malformed JSON.");
//        return ResponseEntity.badRequest().body(errorResponse);
//    }



    // Classe interna para padronizar o retorno
    private static class ErrorResponse {
        private List<Map<String, String>> errors = new ArrayList<>();

        public ErrorResponse(String message) {
            addError(message);
        }

        public ErrorResponse() {}

        public void addError(String message) {
            Map<String, String> error = new HashMap<>();
            error.put("error", message);
            this.errors.add(error);
        }

        public void addFieldError(String field, String message) {
            Map<String, String> error = new HashMap<>();
            error.put(field, message);
            this.errors.add(error);
        }

        public List<Map<String, String>> getErrors() {
            return errors;
        }
    }
}
