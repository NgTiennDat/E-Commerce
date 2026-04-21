package com.eCommerce.file.service.impl;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.file.config.MinioProperties;
import com.eCommerce.file.model.FileUploadResponse;
import com.eCommerce.file.service.FileService;
import com.eCommerce.file.storage.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final StoragePort storagePort;
    private final MinioProperties minioProperties;

    @Override
    public FileUploadResponse upload(String folder, MultipartFile file) {
        validate(file);
        String url = storagePort.upload(folder, file);
        String objectKey = extractObjectKey(url, folder);
        log.info("Uploaded file to folder '{}': {}", folder, objectKey);
        return new FileUploadResponse(objectKey, url);
    }

    @Override
    public List<FileUploadResponse> uploadMultiple(String folder, List<MultipartFile> files) {
        return files.stream()
                .map(file -> upload(folder, file))
                .toList();
    }

    @Override
    public void delete(String objectKey) {
        storagePort.delete(objectKey);
        log.info("Deleted file: {}", objectKey);
    }

    @Override
    public String getPresignedUrl(String objectKey) {
        return storagePort.getPresignedUrl(objectKey);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ResponseCode.FILE_EMPTY);
        }
        if (file.getSize() > minioProperties.getMaxFileSizeBytes()) {
            throw new CustomException(ResponseCode.FILE_TOO_LARGE);
        }
        if (!minioProperties.getAllowedContentTypes().contains(file.getContentType())) {
            throw new CustomException(ResponseCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    private String extractObjectKey(String url, String folder) {
        int idx = url.indexOf(folder);
        return idx >= 0 ? url.substring(idx) : url;
    }
}
