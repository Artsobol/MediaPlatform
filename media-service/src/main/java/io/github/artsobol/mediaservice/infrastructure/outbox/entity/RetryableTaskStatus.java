package io.github.artsobol.mediaservice.infrastructure.outbox.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RetryableTaskStatus {
  PENDING("PENDING"),
  PUBLISHED("PUBLISHED");

  private final String value;
}
