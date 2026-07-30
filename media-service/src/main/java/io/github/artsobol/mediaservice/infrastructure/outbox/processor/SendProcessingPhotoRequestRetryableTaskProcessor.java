package io.github.artsobol.mediaservice.infrastructure.outbox.processor;

import io.github.artsobol.mediaservice.feature.photo.event.PhotoProcessingRequestedEvent;
import io.github.artsobol.mediaservice.infrastructure.messaging.kafka.PhotoKafkaProducer;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTask;
import io.github.artsobol.mediaservice.infrastructure.outbox.mapper.RetryableTaskMapper;
import io.github.artsobol.mediaservice.infrastructure.outbox.service.RetryableTaskService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendProcessingPhotoRequestRetryableTaskProcessor implements RetryableTaskProcessor {

  private final PhotoKafkaProducer kafkaProducer;
  private final RetryableTaskMapper retryableTaskMapper;
  private final RetryableTaskService retryableTaskService;

  @Override
  public void processRetryableTasks(List<RetryableTask> retryableTasks) {
    List<RetryableTask> successfulRetryableTasks = new ArrayList<>();
    for (RetryableTask retryableTask : retryableTasks) {
      var isSuccess = processRetryableTask(retryableTask);
      if (isSuccess) {
        successfulRetryableTasks.add(retryableTask);
      }
    }
    retryableTaskService.markRetryableTasksAsCompleted(successfulRetryableTasks);
  }

  private boolean processRetryableTask(RetryableTask retryableTask) {
    PhotoProcessingRequestedEvent event;

    try {
      event = retryableTaskMapper.convertJsonToEvent(retryableTask.getPayload());
    } catch (RuntimeException ex) {
      log.error("Failed to deserialize retryable task: taskId={}", retryableTask.getId(), ex);
      return false;
    }

    try {
      kafkaProducer.sendToKafka(event).get(30, TimeUnit.SECONDS);
      return true;
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      log.warn("Kafka sending interrupted: eventId={}", event.eventId(), exception);
      return false;
    } catch (ExecutionException | TimeoutException exception) {
      log.warn("Kafka sending failed: eventId={}", event.eventId(), exception);
      return false;
    }
  }
}
