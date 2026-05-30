package io.github.artsobol.mediaservice.feature.photo.service;

import io.github.artsobol.mediaservice.exception.http.NotFoundException;
import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoCreateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoUpdateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.response.PhotoResponse;
import io.github.artsobol.mediaservice.feature.photo.entity.Photo;
import io.github.artsobol.mediaservice.feature.photo.mapper.PhotoMapper;
import io.github.artsobol.mediaservice.feature.photo.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService, PhotoFinder {

    private final PhotoRepository photoRepository;
    private final PhotoMapper photoMapper;

    @Override
    @Transactional
    public PhotoResponse create(PhotoCreateRequest photoCreateRequest) {
        log.info(
                "Creating photo: hasTitle={} hasDescription={} hasPhotoDate={}",
                photoCreateRequest.title() != null && !photoCreateRequest.title().isBlank(),
                photoCreateRequest.description() != null && !photoCreateRequest.description().isBlank(),
                photoCreateRequest.photoDate() != null
        );
        Photo entity = Photo.create();
        entity.updateBody(photoCreateRequest.title(), photoCreateRequest.description(), photoCreateRequest.photoDate());
        Photo saved = photoRepository.save(entity);
        log.info("Photo created: photoId={}", saved.getId());

        return photoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PhotoResponse getById(Long photoId) {
        Photo entity = getByIdOrThrow(photoId);
        return photoMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoResponse> getAll() {
        log.debug("Fetching all photos");
        return photoRepository.findAll().stream().map(photoMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public PhotoResponse updateBody(Long photoId, PhotoUpdateRequest photoUpdateRequest) {
        log.info("Updating photo: photoId={}", photoId);
        Photo entity = getByIdOrThrow(photoId);
        entity.updateBody(photoUpdateRequest.title(), photoUpdateRequest.description(), photoUpdateRequest.photoDate());
        log.info("Photo updated: photoId={}", photoId);

        return photoMapper.toResponse(entity);
    }

    @Override
    public Photo getByIdOrThrow(Long photoId) {
        log.debug("Fetching photo: photoId={}", photoId);
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Photo with id=" + photoId + "  not found"));
    }
}
