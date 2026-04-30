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

        // 동시성 문제를 피하기 위한 index
        int nextIndex = product.getImages().size();

        // 파일 업로드를 트랜잭션 이후로 미루기 위해 파일 정보를 먼저 전달만 하고 실제 파일 업로드는 별도로 처리
        String imageUrl = productImagePort.upload(file, IMAGE_DIRECTORY);

        // 상품 이미지 추가(domain 내에서 처리)
        product.addImage(ProductImage.create(imageUrl, nextIndex, false));

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
