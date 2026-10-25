package sit.int261.int261sampleexam.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> creatErrorResponse(
            HttpStatus status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(status.value(), code, message, req.getRequestURI()));
    }

    // ---- business ----
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> productNotFound(ProductNotFoundException ex, HttpServletRequest req) {
        return creatErrorResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(BrandNotFoundException.class)
    public ResponseEntity<ErrorResponse> brandNotFound(BrandNotFoundException ex, HttpServletRequest req) {
        return creatErrorResponse(HttpStatus.NOT_FOUND, "BRAND_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ErrorResponse> duplicateSku(DuplicateSkuException ex, HttpServletRequest req) {
        return creatErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_SKU", "SKU already exists", req);
    }

    // Database unique constraint violation fallback -> 409 DUPLICATE_SKU
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest req) {
        return creatErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_SKU", "SKU already exists", req);
    }

    // ---- validate ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(400, "VALIDATION_ERROR",
                "Invalid product data", req.getRequestURI());
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            body.addValidationError(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    // Request Error
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            InvalidRequestException.class})
    public ResponseEntity<ErrorResponse> invalidRequest(Exception ex, HttpServletRequest req) {
        return creatErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "invalid request", req);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> methodValidation(HandlerMethodValidationException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(400, "VALIDATION_ERROR",
                "Invalid product data", req.getRequestURI());

        for (var result : ex.getParameterValidationResults()) {
            if (result instanceof org.springframework.validation.method.ParameterErrors errors) {
                for (FieldError fe : errors.getFieldErrors()) {
                    body.addValidationError(fe.getField(), fe.getDefaultMessage());
                }
            }
        }

        if (body.getFieldErrors().isEmpty()) {
            return creatErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "invalid request", req);
        }
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> mediaType(HttpServletRequest req) {
        return creatErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Unsupported media type", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> methodNotAllowed(HttpServletRequest req) {
        return creatErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "Method not allowed", req);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public void notAcceptable(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setStatus(406);
        res.setContentType("application/json");
        res.getWriter().write("{\"status\":406,\"code\":\"NOT_ACCEPTABLE\",\"message\":\"Not acceptable\","
                + "\"path\":\"" + req.getRequestURI() + "\",\"fieldErrors\":[]}");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        return creatErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        return creatErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Unexpected error", req);
    }
}
