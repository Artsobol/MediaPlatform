package io.github.artsobol.mediaservice.feature.photo.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PhotoUpdateRequest(
        @Size(max = 100)
        String title,
        @Size(max = 1000)
        String description,
        LocalDate photoDate
) {
}
