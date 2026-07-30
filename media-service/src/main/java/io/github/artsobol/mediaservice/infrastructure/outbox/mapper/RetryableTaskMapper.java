package io.github.artsobol.mediaservice.infrastructure.outbox.mapper;

import io.github.artsobol.mediaservice.feature.photo.event.PhotoProcessingRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RetryableTaskMapper {

  private final ObjectMapper objectMapper;

  public String convertEventToJson(PhotoProcessingRequestedEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JacksonException ex) {
      throw new IllegalStateException(
          "Failed to serialize photo processing event: " + event.eventId(), ex);
    }
  }

  public PhotoProcessingRequestedEvent convertJsonToEvent(String payload) {
    try {
      return objectMapper.readValue(payload, PhotoProcessingRequestedEvent.class);
    } catch (JacksonException ex) {
      throw new RuntimeException("Failed to deserialize photo processing event", ex);
    }
  }
}
