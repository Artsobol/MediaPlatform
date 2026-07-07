package io.github.artsobol.mediaservice.feature.photo.service;

import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoCreateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoUpdateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.response.PhotoResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {

  PhotoResponse create(PhotoCreateRequest photoCreateRequest, MultipartFile photoFile);

  PhotoResponse getById(Long photoId);

  List<PhotoResponse> getAll();

  PhotoResponse updateBody(Long photoId, PhotoUpdateRequest photoUpdateRequest);

  void delete(Long photoId);
}
