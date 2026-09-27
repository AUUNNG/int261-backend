package sit.int261.exceptiondemo.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================= Custom Exceptions (Inner Classes) =================

    @Getter
    public static class ValidationException extends RuntimeException {
        private final List<ErrorResponse.Violation> violations = new ArrayList<>();

        public ValidationException(String message) {
            super(message);
        }

        public void addViolation(String field, String message) {
            this.violations.add(new ErrorResponse.Violation(field, message));
        }

        public boolean hasViolations() {
            return !this.violations.isEmpty();
        }
    }

    public static class CustomerNotFoundException extends RuntimeException {
        public CustomerNotFoundException(String message) { super(message); }
    }

    public static class InventoryNotFoundException extends RuntimeException {
        public InventoryNotFoundException(String message) { super(message); }
    }

    public static class StaffNotFoundException extends RuntimeException {
        public StaffNotFoundException(String message) { super(message); }
    }

    public static class InventoryUnavailableException extends RuntimeException {
        public InventoryUnavailableException(String message) { super(message); }
    }

    // ================= Exception Handlers =================

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        ErrorResponse response = new ErrorResponse(
                "VALIDATION_FAILED",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                e.getViolations()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException e) {
        ErrorResponse response = new ErrorResponse(
                "CUSTOMER_NOT_FOUND",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInventoryNotFound(InventoryNotFoundException e) {
        ErrorResponse response = new ErrorResponse(
                "INVENTORY_NOT_FOUND",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(StaffNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStaffNotFound(StaffNotFoundException e) {
        ErrorResponse response = new ErrorResponse(
                "STAFF_NOT_FOUND",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InventoryUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleInventoryUnavailable(InventoryUnavailableException e) {
        ErrorResponse response = new ErrorResponse(
                "INVENTORY_UNAVAILABLE",
                e.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ErrorResponse response = new ErrorResponse(
                "MALFORMED_JSON_REQUEST",
                "Malformed JSON request or invalid data type",
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse response = new ErrorResponse(
                "BAD_REQUEST",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
