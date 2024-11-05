package ru.shtamov.emergency_notification_client.extern.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ControllerHandler {

    @ExceptionHandler(NotCorrectFileFormatException.class)
    public ResponseEntity<String> notCorrectFileFormatException(NotCorrectFileFormatException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncorrectNotificationFormatException.class)
    public ResponseEntity<String> incorrectNotificationException(IncorrectNotificationFormatException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<String> noContentException(NoContentException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IsAlreadyExistException.class)
    public ResponseEntity<String> isAlreadyExistException(IsAlreadyExistException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<String> parseException(ParseException e){
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> methodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, String> errorMap = new HashMap<>();

        log.error(e.getMessage());

        e.getBindingResult().getAllErrors().forEach(violation -> {
            String fieldName = ((FieldError) violation).getField();
            String errorMessage = violation.getDefaultMessage();
            errorMap.put(fieldName, errorMessage);
        });
        return errorMap;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errorMap = new HashMap<>();

        log.error(e.getMessage());

        e.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errorMap.put(fieldName, errorMessage);
        });
        return errorMap;
    }
}
