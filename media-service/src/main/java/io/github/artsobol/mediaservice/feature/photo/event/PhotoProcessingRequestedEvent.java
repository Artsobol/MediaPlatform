package io.github.artsobol.mediaservice.feature.photo.event;

import java.time.Instant;
import java.util.UUID;

public record PhotoProcessingRequestedEvent(
    UUID eventId, Long photoId, String originalObjectKey, String contentType, Instant occurredAt) {}
