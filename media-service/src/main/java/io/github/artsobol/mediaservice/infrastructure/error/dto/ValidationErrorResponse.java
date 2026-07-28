package io.github.artsobol.mediaservice.infrastructure.error.dto;

import java.time.Instant;
import java.util.List;

public record ValidationErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationFieldError> errors) {
  public ValidationErrorResponse {
    errors = List.copyOf(errors);
  }
}
