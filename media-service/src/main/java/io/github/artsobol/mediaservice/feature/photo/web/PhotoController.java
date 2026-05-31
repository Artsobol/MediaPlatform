package io.github.artsobol.mediaservice.feature.photo.web;

import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoCreateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoUpdateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.response.PhotoResponse;
import io.github.artsobol.mediaservice.feature.photo.service.PhotoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    @GetMapping("/{photoId}")
    public PhotoResponse getById(@PathVariable("photoId") @Positive Long photoId) {
        return photoService.getById(photoId);
    }

    @GetMapping
    public List<PhotoResponse> getAll() {
        return photoService.getAll();
    }

    @PostMapping
    public ResponseEntity<PhotoResponse> create(
            @RequestPart("metadata") @Valid PhotoCreateRequest photoCreateRequest,
            @RequestPart("file") MultipartFile photoFile
    ) {
        PhotoResponse response = photoService.create(photoCreateRequest, photoFile);
        URI location = URI.create("/api/v1/photos/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{photoId}")
    public PhotoResponse partiallyUpdate(
            @PathVariable("photoId") @Positive Long photoId,
            @RequestBody @Valid PhotoUpdateRequest photoUpdateRequest
    ) {
        return photoService.updateBody(photoId, photoUpdateRequest);
    }
}
