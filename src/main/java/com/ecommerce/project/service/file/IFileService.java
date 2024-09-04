package com.ecommerce.project.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFileService {
    String uploadImage(String path, MultipartFile file) throws IOException;
}
