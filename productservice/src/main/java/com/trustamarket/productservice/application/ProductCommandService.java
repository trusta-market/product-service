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
import com.trustamarket.productservice.domain.category.Category;
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
    // 카테고리별 임계치(`Category.getEffectiveThreshold()`)와 가격을 비교해 검수 필요 여부 결정.
    // - 가격 ≥ 임계치 (고가): PENDING_INSPECTION + InspectionStatus.PENDING
    // - 가격 < 임계치 (저가): ON_SALE 즉시 + InspectionStatus.NONE
    public Product create(UUID sellerId, String title, String description, Integer price, UUID categoryId, List<String> imageUrls) {
        // 1. 카테고리 조회 (없으면 404)
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));

        // 2. 임계치 비교로 검수 필요 여부 동적 결정 (이전엔 true 하드코딩이었음)
        // price 는 Integer 라 nullable — auto-unbox NPE 회피용 명시 가드
        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        boolean requiresInspection = productDomainService.requiresInspection(category, price);
        
        // grade: 초기엔 미정. 검수 통과 시 검수자가 확정.
        Product product = Product.create(
                sellerId,
                categoryId,
                title,
                description,
                price,
                null,
                requiresInspection,
                imageUrls
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
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        // 변경된 카테고리·가격 기준으로 검수 필요 여부 재판정
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));

        boolean needsInspection = productDomainService.requiresInspection(category, price);

        product.update(title, description, price, categoryId, imageUrls, needsInspection);
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
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        // 전이 가능 여부 검증
        if (!newStatus.canTransitionFrom(product.getStatus())) {
            throw new InvalidStatusTransitionException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }

        // 전이 실행 — switch 분기 제거
        newStatus.execute(product);

        return productRepository.save(product);
    }


    // 검수 시작 (검수자용)
    public Product startInspection(UUID productId, UUID inspectorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.startInspection(inspectorId);
        return productRepository.save(product);
    }

    // 검수 완료 처리 — 등급 확정
    public Product completeInspection(UUID productId, ProductGrade inspectedGrade, UUID inspectorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.completeInspection(inspectedGrade, inspectorId);
        Product saved = productRepository.save(product);

        eventPublisher.publishEvent(new ProductInspectedEvent(saved));

        return saved;
    }

    // 검수 불합격 처리 (검수자용)
    public Product failInspection(UUID productId, UUID inspectorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.failInspection(inspectorId);
        return productRepository.save(product);
    }

    // 주문 확정 이벤트 (Kafka order.product.sold-out) 수신 시 호출. 시스템 호출이라 sellerId 검증 X.
    // 멱등성: 이미 SOLD_OUT 인 상품은 no-op (재배달 대비).
    public void markSoldOutByOrder(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() == ProductStatus.SOLD_OUT) {
            return;
        }
        productDomainService.validateStatusTransition(product.getStatus(), ProductStatus.SOLD_OUT);
        product.markSoldOutByOrder();
        productRepository.save(product);
    }
}
