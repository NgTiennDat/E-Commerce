package com.eCommerce.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private long presignedUrlExpirySeconds = 3600;
    private long maxFileSizeBytes = 10485760L;
    private List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/webp", "image/gif");
}
