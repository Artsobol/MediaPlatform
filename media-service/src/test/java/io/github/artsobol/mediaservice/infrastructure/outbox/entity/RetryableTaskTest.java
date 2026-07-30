package io.github.artsobol.mediaservice.infrastructure.outbox.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RetryableTaskTest {

  @Test
  void create_validValues_createsPendingTask() {
    UUID eventId = UUID.randomUUID();
    String payload = "{\"eventId\":\"" + eventId + "\"}";
    RetryableTaskType type = RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST;
    Instant beforeCreation = Instant.now();

    RetryableTask task = RetryableTask.create(eventId, payload, type);

    Instant afterCreation = Instant.now();
    assertThat(task.getId()).isEqualTo(eventId);
    assertThat(task.getPayload()).isEqualTo(payload);
    assertThat(task.getType()).isEqualTo(type);
    assertThat(task.getStatus()).isEqualTo(RetryableTaskStatus.PENDING);
    assertThat(task.getRetryTime()).isBetween(beforeCreation, afterCreation);
  }

  @Test
  void scheduleNextAttempt_newRetryTime_updatesRetryTime() {
    RetryableTask task =
        RetryableTask.create(
            UUID.randomUUID(), "{\"photoId\":1}", RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);
    Instant nextRetryTime = Instant.parse("2026-07-30T10:15:30Z");

    task.scheduleNextAttempt(nextRetryTime);

    assertThat(task.getRetryTime()).isEqualTo(nextRetryTime);
  }
}
