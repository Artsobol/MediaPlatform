package io.github.artsobol.mediaservice.infrastructure.messaging.kafka;

import io.github.artsobol.mediaservice.feature.photo.event.PhotoProcessingRequestedEvent;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoKafkaProducer {

  private final KafkaTemplate<String, PhotoProcessingRequestedEvent> kafkaTemplate;

  public CompletableFuture<SendResult<String, PhotoProcessingRequestedEvent>> sendToKafka(
      PhotoProcessingRequestedEvent event) {
    return kafkaTemplate
        .send("photo.processing.requested.v1", event.photoId().toString(), event)
        .whenComplete(
            (result, exception) -> {
              if (exception != null) {
                log.error(
                    "Failed to send photo processing event: eventId={}, photoId={}",
                    event.eventId(),
                    event.photoId(),
                    exception);
                return;
              }
              log.info(
                  "Photo processing event sent: eventId={}, photoId={}, partition={}, offset={}",
                  event.eventId(),
                  event.photoId(),
                  result.getRecordMetadata().partition(),
                  result.getRecordMetadata().offset());
            });
  }
}
