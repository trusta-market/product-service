package com.trustamarket.productservice.application;

import com.trustamarket.productservice.application.exception.ProductAccessDeniedException;
import com.trustamarket.productservice.application.exception.ProductNotFoundException;
import com.trustamarket.productservice.application.port.ProductImagePort;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductImage;
import com.trustamarket.productservice.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageAppService {

    private final ProductRepository productRepository;
    private final ProductImagePort productImagePort;

    private static final String IMAGE_DIRECTORY = "products";

    // 이미지 업로드 후 상품에 추가
    @Transactional
    public Product addImage(UUID productId, UUID sellerId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException();
        }

        String imageUrl = productImagePort.upload(file, IMAGE_DIRECTORY);
        product.addImage(ProductImage.create(imageUrl, product.getImages().size(),false));

        return productRepository.save(product);
    }

    // 이미지 삭제
    @Transactional
    public Product removeImage(UUID productId, UUID sellerId, UUID imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException();
        }

        product.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .ifPresent(img -> productImagePort.delete(img.getImageUrl()));

        product.removeImage(imageId);
        return productRepository.save(product);
    }

    // 대표이미지 변경
    @Transactional
    public Product changeThumbnail(UUID productId, UUID sellerId, UUID imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException();
        }

        product.changeThumbnail(imageId);
        return productRepository.save(product);
    }

    // 이미지 순서 변경
    @Transactional
    public Product reorderImages(UUID productId, UUID sellerId, List<UUID> imageIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException();
        }

        product.reorderImages(imageIds);
        return productRepository.save(product);
    }
}
