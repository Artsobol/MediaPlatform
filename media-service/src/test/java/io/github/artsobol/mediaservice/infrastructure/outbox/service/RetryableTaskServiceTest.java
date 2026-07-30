package io.github.artsobol.mediaservice.infrastructure.outbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.artsobol.mediaservice.config.retryable.RetryableTaskProperties;
import io.github.artsobol.mediaservice.feature.photo.event.PhotoProcessingRequestedEvent;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTask;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTaskStatus;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTaskType;
import io.github.artsobol.mediaservice.infrastructure.outbox.mapper.RetryableTaskMapper;
import io.github.artsobol.mediaservice.infrastructure.outbox.repository.RetryableTaskRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RetryableTaskServiceTest {

  @Mock private RetryableTaskRepository retryableTaskRepository;
  @Mock private RetryableTaskMapper retryableTaskMapper;

  private RetryableTaskService retryableTaskService;

  @BeforeEach
  void setUp() {
    RetryableTaskProperties properties = new RetryableTaskProperties(100, Duration.ofSeconds(30));

    retryableTaskService =
        new RetryableTaskService(retryableTaskRepository, retryableTaskMapper, properties);
  }

  @Test
  void createRetryableTask_validEvent_savesPendingTask() {
    // given
    PhotoProcessingRequestedEvent event = createEvent();
    String payload = "{\"eventId\":\"" + event.eventId() + "\"}";

    when(retryableTaskMapper.convertEventToJson(event)).thenReturn(payload);

    // when
    retryableTaskService.createRetryableTask(
        event, RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);

    // then
    ArgumentCaptor<RetryableTask> taskCaptor = ArgumentCaptor.forClass(RetryableTask.class);

    verify(retryableTaskRepository).save(taskCaptor.capture());

    RetryableTask savedTask = taskCaptor.getValue();

    assertThat(savedTask.getId()).isEqualTo(event.eventId());
    assertThat(savedTask.getPayload()).isEqualTo(payload);
    assertThat(savedTask.getType()).isEqualTo(RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);
    assertThat(savedTask.getStatus()).isEqualTo(RetryableTaskStatus.PENDING);
    assertThat(savedTask.getRetryTime()).isNotNull();

    verify(retryableTaskMapper).convertEventToJson(event);
  }

  @Test
  void getRetryableTasksForProcessing_tasksExist_schedulesNextAttempt() {
    // given
    RetryableTask task =
        RetryableTask.create(
            UUID.randomUUID(), "payload", RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);

    when(retryableTaskRepository.findRetryableTaskForProcessing(
            eq(RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST),
            eq(RetryableTaskStatus.PENDING),
            any(Instant.class),
            eq(PageRequest.of(0, 100))))
        .thenReturn(List.of(task));

    Instant lowerBoundary = Instant.now().plusSeconds(29);

    // when
    List<RetryableTask> result =
        retryableTaskService.getRetryableTasksForProcessing(
            RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);

    Instant upperBoundary = Instant.now().plusSeconds(31);

    // then
    assertThat(result).containsExactly(task);
    assertThat(task.getRetryTime()).isBetween(lowerBoundary, upperBoundary);
  }

  @Test
  void markRetryableTasksAsCompleted_tasksExist_updatesTheirIds() {
    // given
    RetryableTask firstTask =
        RetryableTask.create(
            UUID.randomUUID(), "first", RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);

    RetryableTask secondTask =
        RetryableTask.create(
            UUID.randomUUID(), "second", RetryableTaskType.SEND_PROCESSING_PHOTO_REQUEST);

    // when
    retryableTaskService.markRetryableTasksAsCompleted(List.of(firstTask, secondTask));

    // then
    verify(retryableTaskRepository)
        .updateStatusByIds(
            List.of(firstTask.getId(), secondTask.getId()), RetryableTaskStatus.PUBLISHED);
  }

  @Test
  void markRetryableTasksAsCompleted_emptyList_doesNotCallRepository() {
    // when
    retryableTaskService.markRetryableTasksAsCompleted(List.of());

    // then
    verifyNoInteractions(retryableTaskRepository);
  }

  private PhotoProcessingRequestedEvent createEvent() {
    return new PhotoProcessingRequestedEvent(
        UUID.randomUUID(), 1L, "photocards/1/original/photo.jpg", "image/jpeg", Instant.now());
  }
}
