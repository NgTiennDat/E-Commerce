package com.eCommerce.file.storage;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.file.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageAdapter implements StoragePort {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build()
                );
                log.info("Created MinIO bucket: {}", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
            throw new CustomException(ResponseCode.STORAGE_UNAVAILABLE);
        }
    }

    @Override
    public String upload(String folder, MultipartFile file) {
        String objectKey = buildObjectKey(folder, file.getOriginalFilename());
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return buildPublicUrl(objectKey);
        } catch (Exception e) {
            log.error("Failed to upload file: {}", objectKey, e);
            throw new CustomException(ResponseCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to delete file: {}", objectKey, e);
            throw new CustomException(ResponseCode.FILE_DELETE_FAILED);
        }
    }

    @Override
    public String getPresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .method(Method.GET)
                            .expiry((int) minioProperties.getPresignedUrlExpirySeconds(), TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for: {}", objectKey, e);
            throw new CustomException(ResponseCode.FILE_NOT_FOUND);
        }
    }

    private String buildObjectKey(String folder, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return folder + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String buildPublicUrl(String objectKey) {
        return minioProperties.getEndpoint() + "/" + minioProperties.getBucketName() + "/" + objectKey;
    }
}
