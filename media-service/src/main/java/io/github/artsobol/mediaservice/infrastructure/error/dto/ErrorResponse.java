package io.github.artsobol.mediaservice.infrastructure.error.dto;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp, int status, String error, String errorCode, String message, String path) {}
