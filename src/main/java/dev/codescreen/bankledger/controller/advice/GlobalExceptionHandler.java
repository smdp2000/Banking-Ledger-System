package dev.codescreen.bankledger.controller.advice;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import dev.codescreen.bankledger.dto.Error;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleAllExceptions(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            return handleValidationExceptions((MethodArgumentNotValidException) ex);
        } else if (ex instanceof HttpMessageNotReadableException) {
            return handleHttpMessageNotReadable((HttpMessageNotReadableException) ex);
        }
        // Default error handling
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Error("An unexpected error occurred", "INTERNAL_SERVER_ERROR"));
    }

    private ResponseEntity<Error> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });
        return new ResponseEntity<>(new Error(String.join(", ", errors), String.valueOf(HttpStatus.BAD_REQUEST.value())), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Error> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String error = "Malformed JSON request";
        if (ex.getCause() instanceof JsonMappingException) {
            error = "JSON parse error, Bad request";
        }
        return ResponseEntity.badRequest().body(new Error(error, String.valueOf(HttpStatus.BAD_REQUEST.value())));
    }
}
