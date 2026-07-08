package io.github.artsobol.mediaservice.feature.photo.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PhotoUpdateRequest(
    @Size(max = 100, message = "{photo.title.size}") String title,
    @Size(max = 1000) String description,
    @PastOrPresent(message = "{photocard.photodate.present}") LocalDate photoDate) {}
