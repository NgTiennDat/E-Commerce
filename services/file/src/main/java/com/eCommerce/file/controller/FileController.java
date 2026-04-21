package com.eCommerce.file.controller;

import com.eCommerce.common.payload.Response;
import com.eCommerce.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("folder") String folder,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(fileService.upload(folder, file)));
    }

    @PostMapping("/upload/batch")
    public ResponseEntity<?> uploadMultiple(
            @RequestParam("folder") String folder,
            @RequestParam("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(fileService.uploadMultiple(folder, files)));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("objectKey") String objectKey) {
        fileService.delete(objectKey);
        return ResponseEntity.ok(Response.ofSucceeded());
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<?> getPresignedUrl(@RequestParam("objectKey") String objectKey) {
        return ResponseEntity.ok(Response.ofSucceeded(fileService.getPresignedUrl(objectKey)));
    }
}
