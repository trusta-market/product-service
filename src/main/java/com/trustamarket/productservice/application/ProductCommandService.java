package com.trustamarket.productservice.application;

import com.trustamarket.productservice.application.event.*;
import com.trustamarket.productservice.application.exception.CategoryNotFoundException;
import com.trustamarket.productservice.application.exception.InvalidStatusTransitionException;
import com.trustamarket.productservice.application.exception.ProductAccessDeniedException;
import com.trustamarket.productservice.application.exception.ProductNotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.application.port.ProductEventPublishPort;
import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.CategoryRepository;
import com.trustamarket.productservice.domain.product.InspectionStatus;
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
    private final ProductEventPublishPort productEventPublishPort;

    // 상품 등록
    // 카테고리별 임계치(`Category.getEffectiveThreshold()`)와 가격을 비교해 검수 필요 여부 결정.
    // - 가격 ≥ 임계치 (고가): PENDING_INSPECTION + InspectionStatus.PENDING
    // - 가격 < 임계치 (저가): ON_SALE 즉시 + InspectionStatus.NONE
    public Product create(UUID sellerId, String title, String description, Long price, UUID categoryId, List<String> imageUrls) {
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

    // 검수 신청 — 판매자가 센터 선택 후 호출. inspection.requested 발행
    public void requestInspection(UUID productId, UUID sellerId, UUID centerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }
        if (product.getInspectionStatus() != InspectionStatus.PENDING) {
            throw new InvalidStatusTransitionException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }
        product.submitForInspection();
        productRepository.save(product);
        productEventPublishPort.publishInspectionRequested(
                productId, sellerId, centerId, product.getPrice(), "KRW"
        );
    }

    // 상품 수정
    public Product update(UUID productId, UUID sellerId, String title,
                          String description, Long price, UUID categoryId, List<String> imageUrls) {

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


// 등급 + 제안가격을 저장하고 PRICE_SUGGESTED 상태로 전환. 판매자 결정 대기.
    public Product receiveInspectionResult(UUID productId, ProductGrade grade,
                                           Long suggestedPrice, UUID inspectorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.receiveInspectionResult(grade, suggestedPrice, inspectorId);
        Product saved = productRepository.save(product);
        eventPublisher.publishEvent(new InspectionResultReceivedEvent(saved)); // ES 재인덱싱
        return saved;
    }

    // 판매자 수락 → price = suggestedPrice, ON_SALE (상품 등록 완료)
    public Product acceptInspectionResult(UUID productId, UUID sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        product.acceptInspectionResult();
        Product saved = productRepository.save(product);
        eventPublisher.publishEvent(new ProductInspectedEvent(saved));       // ES 인덱싱
        eventPublisher.publishEvent(new InspectionAcceptedEvent(saved));     // Kafka 발행
        return saved;
    }

    // 판매자 거절 → INSPECTION_REJECTED
    public Product rejectInspectionResult(UUID productId, UUID sellerId, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        product.rejectInspectionResult();
        Product saved = productRepository.save(product);
        eventPublisher.publishEvent(new InspectionRejectedEvent(saved, reason)); // Kafka 발행
        return saved;
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
