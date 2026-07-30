package io.github.artsobol.mediaservice.infrastructure.outbox.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RetryableTaskType {
  SEND_PROCESSING_PHOTO_REQUEST("SEND PROCESSING PHOTO REQUEST");

  private final String value;
}
