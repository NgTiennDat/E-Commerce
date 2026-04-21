package com.eCommerce.file.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Port (interface) for object storage operations.
 * Decouples business logic from the storage backend (MinIO, S3, GCS, etc.).
 */
public interface StoragePort {

    /**
     * Uploads a file and returns its public URL.
     *
     * @param folder    logical folder/prefix (e.g. "products", "avatars")
     * @param file      the multipart file to upload
     * @return public URL of the uploaded object
     */
    String upload(String folder, MultipartFile file);

    /**
     * Deletes an object by its full object key.
     *
     * @param objectKey full object key (e.g. "products/uuid-filename.jpg")
     */
    void delete(String objectKey);

    /**
     * Generates a pre-signed URL for temporary access.
     *
     * @param objectKey full object key
     * @return pre-signed URL
     */
    String getPresignedUrl(String objectKey);
}
