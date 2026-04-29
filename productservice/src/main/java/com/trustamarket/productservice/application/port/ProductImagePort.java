package com.trustamarket.productservice.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImagePort {
    String upload(MultipartFile file, String directory);
    void delete(String imageUrl);
}
