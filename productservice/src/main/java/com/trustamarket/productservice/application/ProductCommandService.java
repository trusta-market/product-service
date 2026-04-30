package com.trustamarket.productservice.application;

import com.trustamarket.productservice.application.exception.CategoryNotFoundException;
import com.trustamarket.productservice.application.exception.InvalidStatusTransitionException;
import com.trustamarket.productservice.application.exception.ProductAccessDeniedException;
import com.trustamarket.productservice.application.exception.ProductNotFoundException;
import com.trustamarket.productservice.application.port.ProductEventPublishPort;
import com.trustamarket.productservice.application.port.ProductSearchPort;
import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.CategoryRepository;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductDomainService;
import com.trustamarket.productservice.domain.product.ProductGrade;
import com.trustamarket.productservice.domain.product.ProductRepository;
import com.trustamarket.productservice.domain.product.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDomainService productDomainService;
    private final ProductSearchPort productSearchPort;
    private final ProductEventPublishPort productEventPublishPort;

    // 상품 등록
    public Product create(UUID sellerId, String title, String description, Integer price, UUID categoryId) {
        // 1. 카테고리 확인
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // grade: 초기에는 등급이 없으므로 null 전송
        // requiresInspection: 일단 기본값으로 true(검수 필요)를 설정 (프로젝트 정책에 따라 변경)
        Product product = Product.create(
                sellerId,
                categoryId,
                title,
                description,
                price,
                null,   // grade 추가
                true    // requiresInspection 추가
        );

        Product savedProduct = productRepository.save(product);
        productEventPublishPort.publishProductCreated(savedProduct);
        return savedProduct;
    }

    // 상품 수정
    public Product update(UUID productId, UUID sellerId, String title,
                          String description, int price,
                          ProductGrade grade, UUID categoryId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException();
        }

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        product.update(title, description, price, grade, categoryId);
        Product saved = productRepository.save(product);

        productSearchPort.index(saved);

        return saved;
    }

    // 상품 삭제
    public void deleteProduct(UUID productId, UUID sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException();
        }

        productRepository.deleteById(productId);
        productSearchPort.delete(productId);
        productEventPublishPort.publishProductDeleted(productId);
    }

    // 상품 상태 변경
    public Product changeStatus(UUID productId, ProductStatus newStatus, UUID sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException();
        }

        productDomainService.validateStatusTransition(product.getStatus(), newStatus);

        switch (newStatus) {
            case RESERVED -> {
                if (!product.isSaleable()) {      // ← isSaleable() 활성화
                    throw new InvalidStatusTransitionException();
                }
                product.reserve();
            }
            case SOLD_OUT -> product.completeSale();
            case ON_SALE  -> product.cancelReservation();
            default       -> throw new InvalidStatusTransitionException();
        }

        return productRepository.save(product);
    }

    // 검수 시작 (검수자용)
    public Product startInspection(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.startInspection();
        return productRepository.save(product);
    }

    // 검수 완료 처리 — 등급 확정
    public Product completeInspection(UUID productId, ProductGrade inspectedGrade) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.completeInspection(inspectedGrade);
        Product saved = productRepository.save(product);

        productSearchPort.index(saved);

        return saved;
    }

    // 검수 불합격 처리 (검수자용)
    public Product failInspection(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.failInspection();
        return productRepository.save(product);
    }
}