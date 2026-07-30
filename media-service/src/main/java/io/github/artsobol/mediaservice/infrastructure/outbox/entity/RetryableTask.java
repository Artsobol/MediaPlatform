package io.github.artsobol.mediaservice.infrastructure.outbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "retryable_tasks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RetryableTask {

  @Id @Getter private UUID id;

  @Getter
  @Column(nullable = false, name = "payload")
  private String payload;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "type")
  private RetryableTaskType type;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "status")
  private RetryableTaskStatus status;

  @Getter
  @Column(nullable = false, name = "retry_time")
  private Instant retryTime;

  @CreatedDate
  @Column(nullable = false, name = "created_at", updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false, name = "updated_at")
  private Instant updatedAt;

  public static RetryableTask create(UUID eventId, String payload, RetryableTaskType type) {
    RetryableTask task = new RetryableTask();
    task.id = eventId;
    task.payload = payload;
    task.type = type;
    task.status = RetryableTaskStatus.PENDING;
    task.retryTime = Instant.now();

    return task;
  }

  public void scheduleNextAttempt(Instant retryTime) {
    this.retryTime = retryTime;
  }
}
