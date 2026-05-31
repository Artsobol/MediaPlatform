package io.github.artsobol.mediaservice.feature.photo.service;

import io.github.artsobol.mediaservice.config.s3.MinioProperties;
import io.github.artsobol.mediaservice.exception.business.FileStorageException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public void upload(String objectKey, MultipartFile file) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1L)
                    .contentType(file.getContentType())
                    .build());
        } catch (IOException | MinioException e) {
            throw new FileStorageException("Failed to upload file to storage", e);
        }
    }

    public String getPermanentUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(properties.bucket())
                            .object(objectKey)
                            .build()
            );
        } catch (MinioException e) {
            throw new FileStorageException("Failed to generate file download URL", e);
        }
    }

    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(properties.bucket()).object(objectKey).build());
        } catch (MinioException e) {
            throw new FileStorageException("Failed to delete file from S3 storage", e);
        }
    }
}
