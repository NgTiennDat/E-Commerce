package com.eCommerce.file.service;

import com.eCommerce.file.model.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    FileUploadResponse upload(String folder, MultipartFile file);

    List<FileUploadResponse> uploadMultiple(String folder, List<MultipartFile> files);

    void delete(String objectKey);

    String getPresignedUrl(String objectKey);
}
