package io.github.artsobol.mediaservice.feature.photo.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    void upload(String objectKey, MultipartFile file);

    String getPermanentUrl(String objectKey);

    void delete(String objectKey);
}
