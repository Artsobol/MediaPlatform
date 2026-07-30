package io.github.artsobol.mediaservice.infrastructure.outbox.processor;

import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTask;
import java.util.List;

public interface RetryableTaskProcessor {
  void processRetryableTasks(List<RetryableTask> retryableTasks);
}
