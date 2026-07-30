package io.github.artsobol.mediaservice.infrastructure.outbox.repository;

import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTask;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTaskStatus;
import io.github.artsobol.mediaservice.infrastructure.outbox.entity.RetryableTaskType;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RetryableTaskRepository extends JpaRepository<RetryableTask, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
              SELECT r from RetryableTask r where r.type= :type
              AND r.retryTime<= :retryTime
              AND r.status = :status
              order by r.retryTime asc
              """)
  List<RetryableTask> findRetryableTaskForProcessing(
      RetryableTaskType type, RetryableTaskStatus status, Instant retryTime, Pageable pageable);

  @Modifying(clearAutomatically = true)
  @Query(
"""
    UPDATE RetryableTask r SET r.status= :status WHERE r.id IN :ids
""")
  void updateStatusByIds(List<UUID> ids, RetryableTaskStatus status);
}
