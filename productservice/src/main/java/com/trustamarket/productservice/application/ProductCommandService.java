package com.trustamarket.productservice.application;

import com.trustamarket.productservice.application.event.ProductCreatedEvent;
import com.trustamarket.productservice.application.event.ProductDeletedEvent;
import com.trustamarket.productservice.application.event.ProductInspectedEvent;
import com.trustamarket.productservice.application.event.ProductUpdatedEvent;
import com.trustamarket.productservice.application.exception.CategoryNotFoundException;
import com.trustamarket.productservice.application.exception.InvalidStatusTransitionException;
import com.trustamarket.productservice.application.exception.ProductAccessDeniedException;
import com.trustamarket.productservice.application.exception.ProductNotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.domain.category.CategoryRepository;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductDomainService;
import com.trustamarket.productservice.domain.product.ProductGrade;
import com.trustamarket.productservice.domain.product.ProductRepository;
import com.trustamarket.productservice.domain.product.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDomainService productDomainService;
    private final ApplicationEventPublisher eventPublisher;

    // 상품 등록
    public Product create(UUID sellerId, String title, String description, Integer price, UUID categoryId, List<String> imageUrls) {
        // 1. 카테고리 확인
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));

        // grade: 초기에는 등급이 없으므로 null 전송
        // requiresInspection: 일단 기본값으로 true(검수 필요)를 설정 (프로젝트 정책에 따라 변경)
        Product product = Product.create(
                sellerId,
                categoryId,
                title,
                description,
                price,
                null,   // grade 추가
                true,
                imageUrls// requiresInspection 추가
        );

        Product savedProduct = productRepository.save(product);
        eventPublisher.publishEvent(new ProductCreatedEvent(savedProduct));
        return savedProduct;
    }

    // 상품 수정
    public Product update(UUID productId, UUID sellerId, String title,
                          String description, int price, UUID categoryId, List<String> imageUrls) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);        }

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));

        product.update(title, description, price, categoryId, imageUrls);
        Product saved = productRepository.save(product);

        eventPublisher.publishEvent(new ProductUpdatedEvent(saved));

        return saved;
    }

    // 상품 삭제
    public void deleteProduct(UUID productId, UUID sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);        }

        productRepository.deleteById(productId);
        eventPublisher.publishEvent(new ProductDeletedEvent(productId));
    }

    // 상품 상태 변경
    public Product changeStatus(UUID productId, ProductStatus newStatus, UUID sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);        }

        productDomainService.validateStatusTransition(product.getStatus(), newStatus);

        switch (newStatus) {
            case RESERVED -> {
                if (!product.isSaleable()) {      // ← isSaleable() 활성화
                    throw new InvalidStatusTransitionException(ProductErrorCode.INVALID_STATUS_TRANSITION);
                }
                product.reserve();
            }
            case SOLD_OUT -> product.completeSale();
            case ON_SALE  -> {
                if (product.getStatus() == ProductStatus.RESERVED) {
                    product.cancelReservation(); // 예약 중일 때만 예약 취소 로직 실행
                } else {
                    // 예약 상태가 아니라면 도메인 모델에 정의된 일반적인 판매 시작 메서드나 상태 변경 로직 호출
                    product.reopenForSale();
                }
            }
            default -> throw new InvalidStatusTransitionException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }

        return productRepository.save(product);
    }

    // 검수 시작 (검수자용)
    public Product startInspection(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.startInspection();
        return productRepository.save(product);
    }

    // 검수 완료 처리 — 등급 확정
    public Product completeInspection(UUID productId, ProductGrade inspectedGrade) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.completeInspection(inspectedGrade);
        Product saved = productRepository.save(product);

        eventPublisher.publishEvent(new ProductInspectedEvent(saved));

        return saved;
    }

    // 검수 불합격 처리 (검수자용)
    public Product failInspection(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.failInspection();
        return productRepository.save(product);
    }
}