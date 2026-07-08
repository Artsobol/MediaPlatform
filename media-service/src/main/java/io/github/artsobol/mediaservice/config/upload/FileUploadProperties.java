package io.github.artsobol.mediaservice.config.upload;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.upload")
public record FileUploadProperties(
    DataSize maxFileSize, List<String> allowedContentTypes, List<String> allowedExtensions) {
  public FileUploadProperties {
    allowedContentTypes = List.copyOf(allowedContentTypes);
    allowedExtensions = List.copyOf(allowedExtensions);
  }
}
