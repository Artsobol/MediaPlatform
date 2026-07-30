package io.github.artsobol.mediaservice.feature.photo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.artsobol.mediaservice.exception.http.NotFoundException;
import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoCreateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.request.PhotoUpdateRequest;
import io.github.artsobol.mediaservice.feature.photo.dto.response.PhotoResponse;
import io.github.artsobol.mediaservice.feature.photo.entity.Photo;
import io.github.artsobol.mediaservice.feature.photo.mapper.PhotoMapper;
import io.github.artsobol.mediaservice.feature.photo.repository.PhotoRepository;
import io.github.artsobol.mediaservice.infrastructure.error.file.FileValidationService;
import io.github.artsobol.mediaservice.infrastructure.outbox.service.RetryableTaskService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PhotoServiceImplTest {

  private static final String PHOTO_URL =
      "https://storage.example.com/photos/1/original/photo.jpeg";
  private static final Long PHOTO_ID = 1L;
  private static final String ORIGINAL_IMAGE_KEY = "photos/1/original/photo.jpeg";
  @Mock private PhotoRepository photoRepository;
  @Mock private PhotoMapper photoMapper;
  @Mock private S3Service s3Service;
  @Mock private FileValidationService fileValidationService;
  @Mock private RetryableTaskService retryableTaskService;
  @InjectMocks private PhotoServiceImpl photoService;

  @Test
  @DisplayName("getById: photo exists - returns photo response")
  void getById_photoExists_returnsResponse() {
    // given
    Photo photo = createPhoto();
    PhotoResponse expectedResponse = createResponse(photo);

    when(photoRepository.findActiveById(PHOTO_ID)).thenReturn(Optional.of(photo));
    when(photoMapper.toResponse(photo, PHOTO_URL)).thenReturn(expectedResponse);
    when(s3Service.getPermanentUrl(photo.getOriginalImageKey())).thenReturn(PHOTO_URL);

    // when
    PhotoResponse actualResponse = photoService.getById(PHOTO_ID);

    // then
    assertThat(actualResponse).isEqualTo(expectedResponse);

    verify(photoRepository).findActiveById(PHOTO_ID);
    verify(photoMapper).toResponse(photo, PHOTO_URL);
    verify(s3Service).getPermanentUrl(photo.getOriginalImageKey());
  }

  @Test
  @DisplayName("getAll: photos exists - returns photo list")
  void getAll_photosExists_returnsPhotoList() {
    // given
    Photo photo = createPhoto();
    PhotoResponse photoResponse = createResponse(photo);
    List<PhotoResponse> expectedResponse = List.of(photoResponse);

    when(photoRepository.findAllActive()).thenReturn(List.of(photo));
    when(photoMapper.toResponse(photo, PHOTO_URL)).thenReturn(photoResponse);
    when(s3Service.getPermanentUrl(photo.getOriginalImageKey())).thenReturn(PHOTO_URL);

    // when
    List<PhotoResponse> actualResponse = photoService.getAll();

    // then
    assertThat(actualResponse).isEqualTo(expectedResponse);

    verify(photoRepository).findAllActive();
    verify(photoMapper).toResponse(photo, PHOTO_URL);
    verify(s3Service).getPermanentUrl(photo.getOriginalImageKey());
  }

  @Test
  @DisplayName("getAll: no photos exist - returns empty list")
  void getAll_noPhotosExist_returnsEmptyList() {
    // given
    when(photoRepository.findAllActive()).thenReturn(List.of());

    // when
    List<PhotoResponse> actualResponse = photoService.getAll();

    // then
    assertThat(actualResponse).isEmpty();

    verify(photoRepository).findAllActive();
    verifyNoInteractions(photoMapper, s3Service);
  }

  @Test
  @DisplayName("getById: photo does not exists - throws NotFoundException")
  void getById_photoNotExists_returnsNotFoundException() {
    // given
    when(photoRepository.findActiveById(PHOTO_ID)).thenReturn(Optional.empty());

    // when + then
    assertThatThrownBy(() -> photoService.getById(PHOTO_ID)).isInstanceOf(NotFoundException.class);

    verify(photoRepository).findActiveById(PHOTO_ID);
    verifyNoInteractions(s3Service, photoMapper);
  }

  @Test
  @DisplayName("create: valid request - returns photo")
  void createPhoto_validRequest_returnsPhoto() {
    // given
    Photo photo = createPhoto();
    PhotoCreateRequest request =
        new PhotoCreateRequest(photo.getTitle(), photo.getDescription(), photo.getPhotoDate());
    PhotoResponse expectedResponse = createResponse(photo);
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

    when(photoRepository.save(any(Photo.class)))
        .thenAnswer(
            invocation -> {
              Photo saved = invocation.getArgument(0);
              ReflectionTestUtils.setField(saved, "id", PHOTO_ID);
              return saved;
            });
    when(photoMapper.toResponse(any(Photo.class), eq(PHOTO_URL))).thenReturn(expectedResponse);
    when(s3Service.getPermanentUrl(anyString())).thenReturn(PHOTO_URL);

    // when
    PhotoResponse actualResponse = photoService.create(request, file);

    // then
    assertThat(actualResponse).isEqualTo(expectedResponse);

    ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

    verify(fileValidationService).validatePhoto(file);
    verify(photoRepository).save(photoCaptor.capture());
    verify(s3Service).upload(keyCaptor.capture(), eq(file));

    Photo savedPhoto = photoCaptor.getValue();

    assertThat(savedPhoto.getTitle()).isEqualTo(request.title());
    assertThat(savedPhoto.getDescription()).isEqualTo(request.description());
    assertThat(savedPhoto.getPhotoDate()).isEqualTo(request.photoDate());
    assertThat(savedPhoto.getOriginalImageKey()).isEqualTo(keyCaptor.getValue());

    assertThat(keyCaptor.getValue())
        .startsWith("photocards/" + PHOTO_ID + "/original/")
        .endsWith(".jpg");

    verify(s3Service).getPermanentUrl(keyCaptor.getValue());
    verify(photoMapper).toResponse(savedPhoto, PHOTO_URL);
  }

  @Test
  @DisplayName("update: photo exists - returns photo")
  void updatePhoto_photoExists_returnsPhoto() {
    // given
    Photo photo = createPhoto();
    PhotoUpdateRequest request = new PhotoUpdateRequest("new Title", "new desc", null);
    PhotoResponse expectedResponse =
        new PhotoResponse(
            PHOTO_ID,
            PHOTO_URL,
            request.title(),
            request.description(),
            photo.getPhotoDate(),
            photo.getPhotoStatus());
    when(photoRepository.findActiveById(PHOTO_ID)).thenReturn(Optional.of(photo));
    when(photoMapper.toResponse(photo, PHOTO_URL)).thenReturn(expectedResponse);
    when(s3Service.getPermanentUrl(photo.getOriginalImageKey())).thenReturn(PHOTO_URL);

    // when
    PhotoResponse actualResponse = photoService.updateBody(PHOTO_ID, request);

    // then
    assertThat(actualResponse).isEqualTo(expectedResponse);
    assertThat(photo.getTitle()).isEqualTo(request.title());
    assertThat(photo.getDescription()).isEqualTo(request.description());

    verify(photoRepository).findActiveById(PHOTO_ID);
    verify(photoMapper).toResponse(photo, PHOTO_URL);
    verify(s3Service).getPermanentUrl(photo.getOriginalImageKey());
  }

  @Test
  @DisplayName("update: photo not exists - throws Not Found Exception")
  void updatePhoto_photoNotExists_throwsNotFoundException() {
    // given
    when(photoRepository.findActiveById(PHOTO_ID)).thenReturn(Optional.empty());

    // when + then
    assertThatThrownBy(() -> photoService.updateBody(PHOTO_ID, any(PhotoUpdateRequest.class)))
        .isInstanceOf(NotFoundException.class);

    verify(photoRepository).findActiveById(PHOTO_ID);
  }

  @Test
  @DisplayName("delete: photo exists - returns void")
  void deletePhoto_photoExists_returnsVoid() {
    // given
    Photo photo = createPhoto();

    when(photoRepository.findActiveById(PHOTO_ID)).thenReturn(Optional.of(photo));

    // when

    photoService.delete(PHOTO_ID);

    // then
    assertThat(ReflectionTestUtils.getField(photo, "deletedAt")).isNotNull();

    verify(photoRepository).findActiveById(PHOTO_ID);
  }

  @Test
  @DisplayName("delete: photo not exists - throws not found exception")
  void deletePhoto_photoNotExists_throwsNotFoundException() {
    // given
    when(photoRepository.findActiveById(PHOTO_ID)).thenReturn(Optional.empty());

    // when + then
    assertThatThrownBy(() -> photoService.delete(PHOTO_ID)).isInstanceOf(NotFoundException.class);

    verify(photoRepository).findActiveById(PHOTO_ID);
  }

  private Photo createPhoto() {
    Photo photo = Photo.create();
    photo.updateBody("Photo title", "Photo description", LocalDate.of(2020, 2, 2));
    ReflectionTestUtils.setField(photo, "id", 1L);
    photo.updateOriginalImageKey(ORIGINAL_IMAGE_KEY);

    return photo;
  }

  private PhotoResponse createResponse(Photo photo) {
    return new PhotoResponse(
        PHOTO_ID,
        PHOTO_URL,
        photo.getTitle(),
        photo.getDescription(),
        photo.getPhotoDate(),
        photo.getPhotoStatus());
  }
}
