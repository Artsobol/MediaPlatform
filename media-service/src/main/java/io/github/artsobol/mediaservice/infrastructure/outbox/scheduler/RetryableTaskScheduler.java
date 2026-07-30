package io.github.artsobol.mediaservice.infrastructure.outbox.scheduler;

import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTask;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTaskType;
import io.github.artsobol.mediaservice.infrastructure.outbox.processor.SendProcessingPhotoRequestRetryableTaskProcessor;
import io.github.artsobol.mediaservice.infrastructure.outbox.service.RetryableTaskService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryableTaskScheduler {

  private final RetryableTaskService retryableTaskService;
  private final SendProcessingPhotoRequestRetryableTaskProcessor taskProcessor;

  @Scheduled(fixedDelayString = "${app.scheduler-config.delay}")
  public void executeRetryableTasks() {
    log.debug("Starting retryable task processors");
    List<RetryableTask> tasks =
        retryableTaskService.getRetryableTasksForProcessing(
            RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);

    taskProcessor.processRetryableTasks(tasks);
  }
}
