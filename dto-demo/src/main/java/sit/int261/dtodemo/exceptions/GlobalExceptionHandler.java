package sit.int261.dtodemo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(e.getStatusCode().value(), e.getMessage(), request.getDescription(false));
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), "/customers/{id}");
        String message = e.getValue().toString() + " require: " + e.getRequiredType().toString();
        errorResponse.addValidationError(e.getPropertyName(), e.getValue().toString());
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(errorResponse);
    }
}
