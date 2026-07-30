package io.github.artsobol.mediaservice.infrastructure.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.artsobol.mediaservice.feature.photo.event.PhotoProcessingRequestedEvent;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class PhotoKafkaProducerTest {

  private static final String TOPIC = "photo.processing.requested.v1";

  @Mock private KafkaTemplate<String, PhotoProcessingRequestedEvent> kafkaTemplate;

  @InjectMocks private PhotoKafkaProducer producer;

  @Test
  void sendToKafka_validEvent_sendsWithPhotoIdAsKey() {
    // given
    PhotoProcessingRequestedEvent event = createEvent();

    @SuppressWarnings("unchecked")
    SendResult<String, PhotoProcessingRequestedEvent> sendResult = mock(SendResult.class);

    RecordMetadata metadata = mock(RecordMetadata.class);

    when(sendResult.getRecordMetadata()).thenReturn(metadata);
    when(metadata.partition()).thenReturn(1);
    when(metadata.offset()).thenReturn(10L);

    when(kafkaTemplate.send(TOPIC, event.photoId().toString(), event))
        .thenReturn(CompletableFuture.completedFuture(sendResult));

    // when
    CompletableFuture<SendResult<String, PhotoProcessingRequestedEvent>> future =
        producer.sendToKafka(event);

    // then
    assertThat(future.join()).isSameAs(sendResult);

    verify(kafkaTemplate).send(TOPIC, event.photoId().toString(), event);
  }

  @Test
  void sendToKafka_kafkaFailure_returnsFailedFuture() {
    // given
    PhotoProcessingRequestedEvent event = createEvent();
    RuntimeException kafkaException = new RuntimeException("Kafka unavailable");

    CompletableFuture<SendResult<String, PhotoProcessingRequestedEvent>> failedFuture =
        new CompletableFuture<>();

    failedFuture.completeExceptionally(kafkaException);

    when(kafkaTemplate.send(TOPIC, event.photoId().toString(), event)).thenReturn(failedFuture);

    // when
    CompletableFuture<SendResult<String, PhotoProcessingRequestedEvent>> result =
        producer.sendToKafka(event);

    // then
    assertThatThrownBy(result::join)
        .isInstanceOf(CompletionException.class)
        .hasCause(kafkaException);
  }

  private PhotoProcessingRequestedEvent createEvent() {
    return new PhotoProcessingRequestedEvent(
        UUID.randomUUID(), 1L, "photocards/1/original/photo.jpg", "image/jpeg", Instant.now());
  }
}
