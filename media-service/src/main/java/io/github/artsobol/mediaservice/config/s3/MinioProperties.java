package io.github.artsobol.mediaservice.config.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.minio")
public record MinioProperties(
    String endpoint,
    String publicEndpoint,
    String region,
    String accessKey,
    String secretKey,
    String bucket) {
  public MinioProperties {
    if (publicEndpoint == null || publicEndpoint.isBlank()) {
      publicEndpoint = endpoint;
    }
    if (region == null || region.isBlank()) {
      region = "us-east-1";
    }
  }
}
