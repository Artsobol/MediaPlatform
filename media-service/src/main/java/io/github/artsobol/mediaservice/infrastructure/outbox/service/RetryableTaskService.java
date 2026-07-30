package io.github.artsobol.mediaservice.infrastructure.outbox.service;

import io.github.artsobol.mediaservice.config.retryable.RetryableTaskProperties;
import io.github.artsobol.mediaservice.feature.photo.event.PhotoProcessingRequestedEvent;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTask;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTaskStatus;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTaskType;
import io.github.artsobol.mediaservice.infrastructure.outbox.mapper.RetryableTaskMapper;
import io.github.artsobol.mediaservice.infrastructure.outbox.repository.RetryableTaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetryableTaskService {

  private final RetryableTaskRepository retryableTaskRepository;
  private final RetryableTaskMapper retryableTaskMapper;
  private final RetryableTaskProperties retryableTaskProperties;

  @Transactional
  public void createRetryableTask(PhotoProcessingRequestedEvent event, RetryableTaskType type) {
    String payload = retryableTaskMapper.convertEventToJson(event);
    RetryableTask retryableTask = RetryableTask.create(event.eventId(), payload, type);
    retryableTaskRepository.save(retryableTask);
  }

  @Transactional
  public List<RetryableTask> getRetryableTasksForProcessing(RetryableTaskType type) {
    var currentTime = Instant.now();
    Pageable pageable = PageRequest.of(0, retryableTaskProperties.limit());
    List<RetryableTask> retryableTasks =
        retryableTaskRepository.findRetryableTaskForProcessing(
            type, RetryableTaskStatus.PENDING, Instant.now(), pageable);

    for (RetryableTask retryableTask : retryableTasks) {
      retryableTask.scheduleNextAttempt(currentTime.plus(retryableTaskProperties.retryDelay()));
    }

    return retryableTasks;
  }

  @Transactional
  public void markRetryableTasksAsCompleted(List<RetryableTask> retryableTasks) {
    if (retryableTasks.isEmpty()) {
      return;
    }

    List<UUID> ids = retryableTasks.stream().map(RetryableTask::getId).toList();
    retryableTaskRepository.updateStatusByIds(ids, RetryableTaskStatus.PUBLISHED);
  }
}
