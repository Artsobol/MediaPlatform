package io.github.artsobol.mediaservice.infrastructure.outbox.processor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.artsobol.mediaservice.feature.photo.event.PhotoProcessingRequestedEvent;
import io.github.artsobol.mediaservice.infrastructure.messaging.kafka.PhotoKafkaProducer;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTask;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTaskType;
import io.github.artsobol.mediaservice.infrastructure.outbox.mapper.RetryableTaskMapper;
import io.github.artsobol.mediaservice.infrastructure.outbox.service.RetryableTaskService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendProcessingPhotoRequestRetryableTaskProcessorTest {

  @Mock private PhotoKafkaProducer kafkaProducer;
  @Mock private RetryableTaskMapper retryableTaskMapper;
  @Mock private RetryableTaskService retryableTaskService;

  @InjectMocks private SendProcessingPhotoRequestRetryableTaskProcessor processor;

  @Test
  void processRetryableTasks_successfulSending_marksTaskAsCompleted() {
    // given
    RetryableTask task = createTask("valid-payload");
    PhotoProcessingRequestedEvent event = createEvent(task.getId());

    when(retryableTaskMapper.convertJsonToEvent(task.getPayload())).thenReturn(event);
    when(kafkaProducer.sendToKafka(event)).thenReturn(CompletableFuture.completedFuture(null));

    // when
    processor.processRetryableTasks(List.of(task));

    // then
    verify(retryableTaskMapper).convertJsonToEvent(task.getPayload());
    verify(kafkaProducer).sendToKafka(event);
    verify(retryableTaskService).markRetryableTasksAsCompleted(List.of(task));
  }

  @Test
  void processRetryableTasks_failedSending_doesNotMarkTaskAsCompleted() {
    // given
    RetryableTask task = createTask("valid-payload");
    PhotoProcessingRequestedEvent event = createEvent(task.getId());

    CompletableFuture<?> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));

    when(retryableTaskMapper.convertJsonToEvent(task.getPayload())).thenReturn(event);
    when(kafkaProducer.sendToKafka(event)).thenReturn(failedFuture.thenApply(_ -> null));

    // when
    processor.processRetryableTasks(List.of(task));

    // then
    verify(kafkaProducer).sendToKafka(event);
    verify(retryableTaskService).markRetryableTasksAsCompleted(List.of());
  }

  @Test
  void processRetryableTasks_invalidPayload_doesNotSendToKafka() {
    // given
    RetryableTask task = createTask("invalid-payload");

    when(retryableTaskMapper.convertJsonToEvent(task.getPayload()))
        .thenThrow(new IllegalStateException("Invalid JSON"));

    // when
    processor.processRetryableTasks(List.of(task));

    // then
    verifyNoInteractions(kafkaProducer);
    verify(retryableTaskService).markRetryableTasksAsCompleted(List.of());
  }

  @Test
  void processRetryableTasks_mixedResults_marksOnlySuccessfulTask() {
    // given
    RetryableTask successfulTask = createTask("successful");
    RetryableTask failedTask = createTask("failed");

    PhotoProcessingRequestedEvent successfulEvent = createEvent(successfulTask.getId());
    PhotoProcessingRequestedEvent failedEvent = createEvent(failedTask.getId());

    when(retryableTaskMapper.convertJsonToEvent("successful")).thenReturn(successfulEvent);
    when(retryableTaskMapper.convertJsonToEvent("failed")).thenReturn(failedEvent);

    when(kafkaProducer.sendToKafka(successfulEvent))
        .thenReturn(CompletableFuture.completedFuture(null));

    CompletableFuture<?> failedFuture = new CompletableFuture<>();
    failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));

    when(kafkaProducer.sendToKafka(failedEvent)).thenReturn(failedFuture.thenApply(_ -> null));

    // when
    processor.processRetryableTasks(List.of(successfulTask, failedTask));

    // then
    verify(retryableTaskService).markRetryableTasksAsCompleted(List.of(successfulTask));
  }

  private RetryableTask createTask(String payload) {
    return RetryableTask.create(
        UUID.randomUUID(), payload, RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);
  }

  private PhotoProcessingRequestedEvent createEvent(UUID eventId) {
    return new PhotoProcessingRequestedEvent(
        eventId, 1L, "photocards/1/original/photo.jpg", "image/jpeg", Instant.now());
  }
}
