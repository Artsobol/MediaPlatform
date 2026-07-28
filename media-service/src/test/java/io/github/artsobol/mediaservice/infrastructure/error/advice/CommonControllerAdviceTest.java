package io.github.artsobol.mediaservice.infrastructure.error.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.artsobol.mediaservice.infrastructure.error.dto.ErrorResponse;
import io.github.artsobol.mediaservice.infrastructure.error.dto.ValidationErrorResponse;
import io.github.artsobol.mediaservice.infrastructure.error.dto.ValidationFieldError;
import io.github.artsobol.mediaservice.infrastructure.error.file.FileValidationService;
import io.github.artsobol.mediaservice.infrastructure.error.localization.MessageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ExtendWith(MockitoExtension.class)
class CommonControllerAdviceTest {

  @Mock private MessageService messageService;
  @Mock private FileValidationService fileValidationService;

  private CommonControllerAdvice advice;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    advice = new CommonControllerAdvice(messageService, fileValidationService);

    request = new MockHttpServletRequest();
    request.setRequestURI("/photos");
    request.setMethod("POST");
  }

  @Test
  void handleConstraintViolation_returnsFieldErrors() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path propertyPath = mock(Path.class);

    when(propertyPath.toString()).thenReturn("createPhoto.photoId");
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(violation.getMessage()).thenReturn("must be positive");
    when(messageService.createMessage("validation.error", null)).thenReturn("Validation error");

    ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

    ResponseEntity<ValidationErrorResponse> response =
        advice.handleConstraintViolationException(exception, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().path()).isEqualTo("/photos");
    assertThat(response.getBody().errors())
        .containsExactly(new ValidationFieldError("photoId", "must be positive"));
  }

  @Test
  void handleConstraintViolation_pathWithoutSeparator_returnsFullPathAsFieldName() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path propertyPath = mock(Path.class);

    when(propertyPath.toString()).thenReturn("photoId");
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(violation.getMessage()).thenReturn("must be positive");
    when(messageService.createMessage("validation.error", null)).thenReturn("Validation error");

    ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

    ResponseEntity<ValidationErrorResponse> response =
        advice.handleConstraintViolationException(exception, request);

    assertThat(Objects.requireNonNull(response.getBody()).errors())
        .containsExactly(new ValidationFieldError("photoId", "must be positive"));
  }

  @Test
  void handleConstraintViolation_pathEndingWithSeparator_returnsFullPathAsFieldName() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path propertyPath = mock(Path.class);

    when(propertyPath.toString()).thenReturn("createPhoto.");
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(violation.getMessage()).thenReturn("must be positive");
    when(messageService.createMessage("validation.error", null)).thenReturn("Validation error");

    ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

    ResponseEntity<ValidationErrorResponse> response =
        advice.handleConstraintViolationException(exception, request);

    assertThat(Objects.requireNonNull(response.getBody()).errors())
        .containsExactly(new ValidationFieldError("createPhoto.", "must be positive"));
  }

  @Test
  void handleUnexpectedException_returns500() {
    when(messageService.createMessage("unexpected.error", null)).thenReturn("Unexpected error");

    ResponseEntity<ErrorResponse> response =
        advice.handleException(new RuntimeException("database unavailable"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(Objects.requireNonNull(response.getBody()).errorCode())
        .isEqualTo("INTERNAL_SERVER_ERROR");
    assertThat(response.getBody().message()).isEqualTo("Unexpected error");
  }

  @Test
  void handleMissingParameter_returns400() {
    when(messageService.createMessage(eq("parameter.missing"), any()))
        .thenReturn("Parameter page is required");
    when(messageService.createMessage("parameter.missing.base", null))
        .thenReturn("Required parameters are missing");

    var exception = new MissingServletRequestParameterException("page", "int");

    ResponseEntity<ValidationErrorResponse> response =
        advice.handleMissingServletRequestParameterException(exception, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(Objects.requireNonNull(response.getBody()).errors().getFirst().field())
        .isEqualTo("page");
  }

  @Test
  void handleMaxUploadSizeExceeded_returns400() {
    when(fileValidationService.getMaxFileSizeLabel()).thenReturn("10 MB");
    when(messageService.createMessage(eq("file.too.large"), any()))
        .thenReturn("Maximum file size is 10 MB");
    when(messageService.createMessage("validation.error", null)).thenReturn("Validation error");

    ResponseEntity<ValidationErrorResponse> response =
        advice.handleMaxUploadSizeExceededException(
            new MaxUploadSizeExceededException(11 * 1024 * 1024), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(Objects.requireNonNull(response.getBody()).errors())
        .containsExactly(new ValidationFieldError("file", "Maximum file size is 10 MB"));
  }
}
