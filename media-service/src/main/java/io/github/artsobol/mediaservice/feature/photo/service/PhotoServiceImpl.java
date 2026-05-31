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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.io.FilenameUtils.getExtension;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService, PhotoFinder {

    private final PhotoRepository photoRepository;
    private final PhotoMapper photoMapper;
    private final S3Service s3Service;

    @Override
    @Transactional
    public PhotoResponse create(PhotoCreateRequest photoCreateRequest, MultipartFile photoFile) {
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

        log.info("Uploading photo: photoId={} photoStatus={}", saved.getId(), saved.getPhotoStatus());
        String extension = getExtension(photoFile.getOriginalFilename());
        String originalImageKey = "photocards/%d/original/%s.%s".formatted(saved.getId(), UUID.randomUUID(), extension);

        s3Service.upload(originalImageKey, photoFile);
        saved.updateOriginalImageKey(originalImageKey);
        log.info(
                "Photo uploaded: photoId={} originalImageKey={} photoStatus={}",
                saved.getId(),
                saved.getOriginalImageKey(),
                saved.getPhotoStatus()
        );

        return getResponseWithUrl(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PhotoResponse getById(Long photoId) {
        Photo entity = getByIdOrThrow(photoId);
        return getResponseWithUrl(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoResponse> getAll() {
        log.debug("Fetching all photos");
        return photoRepository.findAll().stream().map(this::getResponseWithUrl).toList();
    }

    @Override
    @Transactional
    public PhotoResponse updateBody(Long photoId, PhotoUpdateRequest photoUpdateRequest) {
        log.info("Updating photo: photoId={}", photoId);
        Photo entity = getByIdOrThrow(photoId);
        entity.updateBody(photoUpdateRequest.title(), photoUpdateRequest.description(), photoUpdateRequest.photoDate());
        log.info("Photo updated: photoId={}", photoId);
        String url = s3Service.getPermanentUrl(entity.getOriginalImageKey());

        return photoMapper.toResponse(entity, url);
    }

    @Override
    public Photo getByIdOrThrow(Long photoId) {
        log.debug("Fetching photo: photoId={}", photoId);
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Photo with id=" + photoId + "  not found"));
    }

    private PhotoResponse getResponseWithUrl(Photo entity) {
        log.debug("Fetching permanent photo url: photoId={}", entity.getId());
        String url = s3Service.getPermanentUrl(entity.getOriginalImageKey());

        return photoMapper.toResponse(entity, url);
    }


}
