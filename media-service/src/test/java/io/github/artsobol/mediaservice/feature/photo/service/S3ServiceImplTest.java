package io.github.artsobol.mediaservice.feature.photo.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.artsobol.mediaservice.config.s3.MinioProperties;
import io.github.artsobol.mediaservice.exception.business.FileStorageException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class S3ServiceImplTest {

  @Mock private MinioClient minioClient;

  @Mock private MinioClient publicMinioClient;

  private S3ServiceImpl s3Service;

  @BeforeEach
  void setUp() {
    MinioProperties properties =
        new MinioProperties(
            "http://minio:9000",
            "http://localhost:9000",
            "us-east-1",
            "access-key",
            "secret-key",
            "media");

    s3Service = new S3ServiceImpl(minioClient, publicMinioClient, properties);
  }

  @Test
  @DisplayName("upload: valid file - sends file to storage")
  void upload_validFile_putsObjectIntoStorage() throws Exception {
    // given
    String objectKey = "photos/1/original/photo.jpg";
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image content".getBytes());

    // when
    s3Service.upload(objectKey, file);

    // then
    ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);

    verify(minioClient).putObject(captor.capture());

    PutObjectArgs args = captor.getValue();

    assertThat(args.bucket()).isEqualTo("media");
    assertThat(args.object()).isEqualTo(objectKey);
    assertThat(args.contentType().toString()).isEqualTo("image/jpeg");
  }

  @Test
  @DisplayName("upload: storage error - throws FileStorageException")
  void upload_storageError_throwsFileStorageException() throws Exception {
    // given
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image content".getBytes());
    when(minioClient.putObject(any(PutObjectArgs.class)))
        .thenThrow(new MinioException("Storage unavailable"));

    // when + then
    assertThatThrownBy(() -> s3Service.upload("photo.jpg", file))
        .isInstanceOf(FileStorageException.class)
        .hasMessage("Failed to upload file to storage")
        .hasCauseInstanceOf(MinioException.class);
  }

  @Test
  @DisplayName("getPermanentUrl: object exists - returns URL")
  void getPermanentUrl_objectExists_returnsUrl() throws Exception {
    // given
    String objectKey = "photos/1/original/photo.jpg";
    String expectedUrl = "http://localhost:9000/media/photo.jpg";
    when(publicMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenReturn(expectedUrl);

    // when
    String actualUrl = s3Service.getPermanentUrl(objectKey);

    // then
    assertThat(actualUrl).isEqualTo(expectedUrl);
    verify(publicMinioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
  }

  @Test
  @DisplayName("getPermanentUrl: object not exists - throws File Storage Exception")
  void getPermanentUrl_objectNotExists_throwsFileStorageException() throws Exception {
    // given
    String objectKey = "photos/1/original/photo.jpg";
    when(publicMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
        .thenThrow(new MinioException("Object not found"));

    // when + then
    assertThatThrownBy(() -> s3Service.getPermanentUrl(objectKey))
        .isInstanceOf(FileStorageException.class)
        .hasMessage("Failed to generate file download URL")
        .hasCauseInstanceOf(MinioException.class);
  }

  @Test
  @DisplayName("delete: object exists - returns void")
  void delete_objectExists_returnsVoid() throws Exception {
    // given
    String objectKey = "photos/1/original/photo.jpg";

    // when
    s3Service.delete(objectKey);

    // then
    verify(minioClient).removeObject(any(RemoveObjectArgs.class));
  }

  @Test
  @DisplayName("delete: object not exists - throws File Storage Exception")
  void delete_storageError_throwsFileStorageException() throws Exception {
    // given
    String objectKey = "photos/1/original/photo.jpg";
    doThrow(new MinioException("Storage unavailable"))
        .when(minioClient)
        .removeObject(any(RemoveObjectArgs.class));

    // when + then
    assertThatThrownBy(() -> s3Service.delete(objectKey))
        .isInstanceOf(FileStorageException.class)
        .hasMessage("Failed to delete file from S3 storage")
        .hasCauseInstanceOf(MinioException.class);
  }
}
