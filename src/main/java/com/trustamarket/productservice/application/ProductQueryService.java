package com.trustamarket.productservice.application;

import com.trustamarket.productservice.application.exception.ProductNotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductCachePort;
import com.trustamarket.productservice.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductCachePort productCache;

    // 상품 단건 조회 (이미지 포함)
    public Product findById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleted()) {
            throw new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    public Product findByIdInternal(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    // 판매자별 상품 목록
    public Page<Product> findBySellerId(UUID sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable);
    }

    // 카테고리별 상품 목록
    public Page<Product> findByCategoryId(UUID categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    // 최신 상품 10개 — read 부담 큰 endpoint. cache hit 시 DB 호출 X.
    // cache 구현체 (Caffeine / Redis 등) 는 ProductCachePort adapter 가 결정.
    public List<Product> findLatest() {
        return productCache.getLatest()
                .orElseGet(() -> {
                    List<Product> latest = productRepository.findTop10ByOrderByCreatedAtDesc();
                    productCache.putLatest(latest);
                    return latest;
                });
    }
}
