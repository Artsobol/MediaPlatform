package io.github.artsobol.mediaservice.feature.photo.dto.response;

import io.github.artsobol.mediaservice.feature.photo.entity.PhotoStatus;

import java.time.LocalDate;

public record PhotoResponse(
        Long id,
        String url,
        String title,
        String description,
        LocalDate photoDate,
        PhotoStatus photoStatus
) {
}
