package io.github.artsobol.mediaservice.infrastructure.error.advice;

import io.github.artsobol.mediaservice.exception.base.BaseException;
import io.github.artsobol.mediaservice.infrastructure.error.dto.ErrorResponse;
import io.github.artsobol.mediaservice.infrastructure.error.dto.ValidationErrorResponse;
import io.github.artsobol.mediaservice.infrastructure.error.dto.ValidationFieldError;
import io.github.artsobol.mediaservice.infrastructure.error.file.FileValidationService;
import io.github.artsobol.mediaservice.infrastructure.error.localization.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonControllerAdvice {

  private final MessageService messageService;
  private final FileValidationService fileValidationService;

  private static @NonNull ErrorResponse getErrorResponse(
      HttpServletRequest request, HttpStatus status, String errorCode, String message) {
    return new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        errorCode,
        message,
        request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ValidationFieldError> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                err ->
                    new ValidationFieldError(
                        err.getField(), messageService.resolveValidationMessage(err)))
            .toList();

    ValidationErrorResponse response = buildValidationErrorResponse(request, errors);
    log.warn("Validation error for request URI: {}. Errors: {}", request.getRequestURI(), errors);

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {
    List<ValidationFieldError> errors =
        ex.getConstraintViolations().stream()
            .map(
                violation ->
                    new ValidationFieldError(extractFieldName(violation), violation.getMessage()))
            .toList();

    ValidationErrorResponse response = buildValidationErrorResponse(request, errors);
    log.warn(
        "Constraint validation error for request URI: {}. Errors: {}",
        request.getRequestURI(),
        errors);

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ValidationErrorResponse> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    List<String> missingParams = Arrays.asList(ex.getParameterName().split(","));
    List<ValidationFieldError> errors =
        missingParams.stream()
            .map(
                param -> {
                  String localizedMessage =
                      messageService.createMessage("parameter.missing", new Object[] {param});
                  return new ValidationFieldError(param, localizedMessage);
                })
            .toList();

    String message = messageService.createMessage("parameter.missing.base", null);

    ValidationErrorResponse response =
        new ValidationErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            errors);

    log.warn(
        "Missing request parameters for URI: {}. Missing parameters: {}",
        request.getRequestURI(),
        missingParams);

    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBaseException(
      BaseException ex, HttpServletRequest request) {
    HttpStatus status = ex.getStatus();
    String message = messageService.createMessage(ex.getMessageKey(), ex.getMessageArgs());

    ErrorResponse response = getErrorResponse(request, status, ex.getErrorCode(), message);
    log.warn(
        "Request failed: method={}, URI={}, status={}, error code={}",
        request.getMethod(),
        request.getRequestURI(),
        status.value(),
        ex.getErrorCode());

    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String message = messageService.createMessage("unexpected.error", null);

    ErrorResponse response = getErrorResponse(request, status, "INTERNAL_SERVER_ERROR", message);
    log.error(
        "Unexpected error: method={}, URI={}, status={}, errorCode={}",
        request.getMethod(),
        request.getRequestURI(),
        500,
        "INTERNAL_SERVER_ERROR",
        ex);

    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ValidationErrorResponse> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String maxFileSize = fileValidationService.getMaxFileSizeLabel();
    String fieldMessage =
        messageService.createMessage("file.too.large", new Object[] {maxFileSize});
    String message = messageService.createMessage("validation.error", null);

    ValidationErrorResponse response =
        new ValidationErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            List.of(new ValidationFieldError("file", fieldMessage)));

    log.warn(
        "Multipart upload is too large: method={}, URI={}, maxFileSize={}",
        request.getMethod(),
        request.getRequestURI(),
        maxFileSize);

    return ResponseEntity.status(status).body(response);
  }

  private ValidationErrorResponse buildValidationErrorResponse(
      HttpServletRequest request, List<ValidationFieldError> errors) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = messageService.createMessage("validation.error", null);

    return new ValidationErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        errors);
  }

  private String extractFieldName(ConstraintViolation<?> violation) {
    String propertyPath = violation.getPropertyPath().toString();
    int separatorIndex = propertyPath.lastIndexOf('.');

    if (separatorIndex >= 0 && separatorIndex < propertyPath.length() - 1) {
      return propertyPath.substring(separatorIndex + 1);
    }

    return propertyPath;
  }
}
