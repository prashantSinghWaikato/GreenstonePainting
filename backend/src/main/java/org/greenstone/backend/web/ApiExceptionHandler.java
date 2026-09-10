package org.greenstone.backend.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.UNAUTHORIZED.value(),
                "Email or password is incorrect.",
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        var fieldErrors = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        var body = new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                "Please check the highlighted fields.",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
        var body = new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                java.util.Map.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(WorkflowConflictException.class)
    public ResponseEntity<ApiError> handleWorkflowConflict(WorkflowConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(StaffAccountConflictException.class)
    public ResponseEntity<ApiError> handleStaffAccountConflict(StaffAccountConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(AccountSecurityException.class)
    public ResponseEntity<ApiError> handleAccountSecurity(AccountSecurityException exception) {
        return ResponseEntity.badRequest().body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(AdminAccountLockedException.class)
    public ResponseEntity<ApiError> handleAccountLocked(AdminAccountLockedException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                exception.getMessage(),
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(UploadValidationException.class)
    public ResponseEntity<ApiError> handleUploadValidation(UploadValidationException exception) {
        return ResponseEntity.badRequest().body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize() {
        return ResponseEntity.badRequest().body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                "Each photo must be 5 MB or smaller.",
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(NotificationDeliveryException.class)
    public ResponseEntity<ApiError> handleNotificationDelivery(NotificationDeliveryException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                exception.getMessage(),
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(QuoteValidationException.class)
    public ResponseEntity<ApiError> handleQuoteValidation(QuoteValidationException exception) {
        return ResponseEntity.badRequest().body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                java.util.Map.of()
        ));
    }

    @ExceptionHandler(JobValidationException.class)
    public ResponseEntity<ApiError> handleJobValidation(JobValidationException exception) {
        return ResponseEntity.badRequest().body(new ApiError(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                java.util.Map.of()
        ));
    }
}
