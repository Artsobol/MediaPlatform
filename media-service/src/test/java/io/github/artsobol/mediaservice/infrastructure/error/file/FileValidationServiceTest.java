package io.github.artsobol.mediaservice.infrastructure.error.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.github.artsobol.mediaservice.config.upload.FileUploadProperties;
import io.github.artsobol.mediaservice.exception.http.BadRequestException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

class FileValidationServiceTest {

  private FileValidationService fileValidationService;

  @BeforeEach
  void setUp() {
    FileUploadProperties properties =
        new FileUploadProperties(
            DataSize.ofMegabytes(10),
            List.of("image/jpeg", "image/png", "image/webp"),
            List.of(".jpg", ".jpeg", ".png", ".webp"));

    fileValidationService = new FileValidationService(properties);
  }

  @Test
  void validatePhoto_validFile_doesNotThrowException() {
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.JPG", "image/jpeg", new byte[] {1, 2, 3});

    assertDoesNotThrow(() -> fileValidationService.validatePhoto(file));
  }

  @Test
  void validatePhoto_nullFile_throwsBadRequestException() {
    assertThatThrownBy(() -> fileValidationService.validatePhoto(null))
        .isInstanceOfSatisfying(
            BadRequestException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo("FILE_EMPTY"));
  }

  @Test
  void validatePhoto_emptyFile_throwsBadRequestException() {
    MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

    assertThatThrownBy(() -> fileValidationService.validatePhoto(file))
        .isInstanceOfSatisfying(
            BadRequestException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo("FILE_EMPTY"));
  }

  @Test
  void validatePhoto_invalidContentType_throwsBadRequestException() {
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.jpg", "application/pdf", new byte[] {1, 2, 3});

    assertThatThrownBy(() -> fileValidationService.validatePhoto(file))
        .isInstanceOfSatisfying(
            BadRequestException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo("FILE_INVALID_TYPE"));
  }

  @Test
  void validatePhoto_invalidExtension_throwsBadRequestException() {
    MockMultipartFile file =
        new MockMultipartFile("file", "photo.exe", "image/jpeg", new byte[] {1, 2, 3});

    assertThatThrownBy(() -> fileValidationService.validatePhoto(file))
        .isInstanceOfSatisfying(
            BadRequestException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo("FILE_INVALID_EXTENSION"));
  }

  @Test
  void getMaxFileSizeLabel_sizeInMegabytes_returnsFormattedValue() {
    assertThat(fileValidationService.getMaxFileSizeLabel()).isEqualTo("10 MB");
  }
}
