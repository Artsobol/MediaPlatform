package io.github.artsobol.mediaservice.infrastructure.error.file;

import io.github.artsobol.mediaservice.config.upload.FileUploadProperties;
import io.github.artsobol.mediaservice.exception.http.BadRequestException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileValidationService {

  private final FileUploadProperties fileUploadProperties;

  public void validatePhoto(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("FILE_EMPTY", "file.empty");
    }

    String contentType = normalize(file.getContentType());
    if (!containsIgnoreCase(fileUploadProperties.allowedContentTypes(), contentType)) {
      throw new BadRequestException(
          "FILE_INVALID_TYPE",
          "file.invalid.type",
          String.join(", ", fileUploadProperties.allowedExtensions()));
    }

    String extension = extractExtension(file.getOriginalFilename());
    if (!containsIgnoreCase(fileUploadProperties.allowedExtensions(), extension)) {
      throw new BadRequestException(
          "FILE_INVALID_EXTENSION",
          "file.invalid.type",
          String.join(", ", fileUploadProperties.allowedExtensions()));
    }
  }

  public String getMaxFileSizeLabel() {
    long bytes = fileUploadProperties.maxFileSize().toBytes();
    long megabytes = bytes / (1024 * 1024);

    if (bytes % (1024 * 1024) == 0) {
      return megabytes + " MB";
    }

    long kilobytes = bytes / 1024;
    if (bytes % 1024 == 0) {
      return kilobytes + " KB";
    }

    return bytes + " B";
  }

  private boolean containsIgnoreCase(List<String> values, String candidate) {
    if (candidate == null) {
      return false;
    }

    return values.stream().anyMatch(value -> value.equalsIgnoreCase(candidate));
  }

  private String extractExtension(String fileName) {
    if (!StringUtils.hasText(fileName)) {
      return null;
    }

    String normalizedFileName = fileName.trim();
    int dotIndex = normalizedFileName.lastIndexOf('.');

    if (dotIndex < 0 || dotIndex == normalizedFileName.length() - 1) {
      return null;
    }

    return normalizedFileName.substring(dotIndex).toLowerCase(Locale.ROOT);
  }

  private String normalize(String value) {
    return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
  }
}
