package io.github.artsobol.mediaservice.feature.photo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PhotoCreateRequest(
    @Size(max = 100, message = "{photo.title.size}") @NotBlank(message = "{photo.title.blank}") String title,
    @Size(max = 1000) String description,
    @PastOrPresent(message = "{photocard.photodate.present}") LocalDate photoDate) {}
