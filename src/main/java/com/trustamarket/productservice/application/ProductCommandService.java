package com.trustamarket.productservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustamarket.productservice.application.exception.CategoryNotFoundException;
import com.trustamarket.productservice.application.exception.InvalidStatusTransitionException;
import com.trustamarket.productservice.application.exception.ProductAccessDeniedException;
import com.trustamarket.productservice.application.exception.ProductNotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.CategoryRepository;
import com.trustamarket.productservice.domain.product.InspectionStatus;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductDomainService;
import com.trustamarket.productservice.domain.product.ProductGrade;
import com.trustamarket.productservice.domain.product.ProductRepository;
import com.trustamarket.productservice.domain.product.ProductStatus;
import com.trustamarket.productservice.infrastructure.kafka.KafkaTopics;
import com.trustamarket.productservice.application.port.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
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
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

     // 명령용 활성 상품 조회 — 소프트 삭제된 상품은 404 처리, 판매자가 직접 호출하는 모든 명령 메서드에서 사용
    private Product findActiveProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));
        if (product.isDeleted()) {
            throw new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

     // 시스템 내부용 조회 — Kafka 이벤트 처리 등 deleted 무관하게 조회
    private Product findProductInternal(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    // 상품 등록
    public Product create(UUID sellerId, String title, String description, Long price, UUID categoryId, List<String> imageUrls) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));

        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        boolean requiresInspection = productDomainService.requiresInspection(category, price);

        Product product = Product.create(
                sellerId, categoryId, title, description, price, null, requiresInspection, imageUrls
        );

        Product savedProduct = productRepository.save(product);

        saveOutboxEvent(KafkaTopics.PRODUCT_CREATED_TOPIC,
                new KafkaTopics.ProductCreatedEvent(
                        savedProduct.getProductId(),
                        savedProduct.getSellerId(),
                        savedProduct.getCategoryId(),
                        savedProduct.getPrice(),
                        savedProduct.getInspectionStatus().name()
                )
        );
        return savedProduct;
    }

    // 검수 신청 — 삭제된 상품 차단
    public void requestInspection(UUID productId, UUID sellerId, UUID centerId) {
        Product product = findActiveProduct(productId);  // findById → findActiveProduct
        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }
        if (product.getInspectionStatus() != InspectionStatus.PENDING) {
            throw new InvalidStatusTransitionException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }
        product.submitForInspection();
        productRepository.save(product);
        saveOutboxEvent(KafkaTopics.INSPECTION_REQUESTED_TOPIC,
                new KafkaTopics.InspectionRequestedEvent(
                        UUID.randomUUID(), productId, sellerId, centerId, product.getPrice(), "KRW"
                )
        );
    }

    // 상품 수정 — 삭제된 상품 차단
    public Product update(UUID productId, UUID sellerId, String title,
                          String description, Long price, UUID categoryId, List<String> imageUrls) {
        Product product = findActiveProduct(productId);  // findById → findActiveProduct
        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));

        boolean needsInspection = productDomainService.requiresInspection(category, price);
        product.update(title, description, price, categoryId, imageUrls, needsInspection);
        return productRepository.save(product);
    }

    // 상품 삭제 — 이미 삭제된 상품 재삭제 차단
    public void deleteProduct(UUID productId, UUID sellerId) {
        Product product = findActiveProduct(productId);
        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }
        product.softDelete();
        productRepository.save(product);
        saveOutboxEvent(KafkaTopics.PRODUCT_DELETED_TOPIC,
                new KafkaTopics.ProductDeletedEvent(productId));
    }

    // 상품 상태 변경 — 삭제된 상품 차단
    public Product changeStatus(UUID productId, ProductStatus newStatus, UUID sellerId) {
        Product product = findActiveProduct(productId);  // findById → findActiveProduct
        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }
        if (!newStatus.canTransitionFrom(product.getStatus())) {
            throw new InvalidStatusTransitionException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }
        newStatus.execute(product);
        return productRepository.save(product);
    }

    // 검수 결과 수신 — 삭제된 상품 차단
    public Product receiveInspectionResult(UUID productId, ProductGrade grade,
                                           Long suggestedPrice, UUID inspectorId) {
        Product product = findProductInternal(productId);
        product.receiveInspectionResult(grade, suggestedPrice, inspectorId);
        return productRepository.save(product);
    }

    // 판매자 수락 — 삭제된 상품 차단
    public Product acceptInspectionResult(UUID productId, UUID sellerId) {
        Product product = findActiveProduct(productId);  // findById → findActiveProduct
        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }
        product.acceptInspectionResult();
        Product saved = productRepository.save(product);
        saveOutboxEvent(KafkaTopics.INSPECTION_PRICE_ACCEPTED_TOPIC,
                new KafkaTopics.InspectionPriceAcceptedEvent(
                        UUID.randomUUID(),
                        saved.getProductId(),
                        saved.getSellerId(),
                        saved.getPrice()
                )
        );        return saved;
    }

    // 판매자 거절 — 삭제된 상품 차단
    public Product rejectInspectionResult(UUID productId, UUID sellerId, String reason) {
        Product product = findActiveProduct(productId);  // findById → findActiveProduct
        if (!product.isOwnedBy(sellerId)) {
            throw new ProductAccessDeniedException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }
        product.rejectInspectionResult();
        Product saved = productRepository.save(product);
        saveOutboxEvent(KafkaTopics.INSPECTION_PRICE_REJECTED_TOPIC,
                new KafkaTopics.InspectionPriceRejectedEvent(
                        UUID.randomUUID(),
                        saved.getProductId(),
                        saved.getSellerId(),
                        reason
                )
        );        return saved;
    }

    // 주문 확정 이벤트 수신 — Kafka 내부 처리이므로 deleted 무관 조회 유지
    // 멱등성: 이미 SOLD_OUT인 상품은 no-op
    public void markSoldOutByOrder(UUID productId) {
        Product product = findProductInternal(productId);  // 시스템 내부용 — deleted 무관
        if (product.getStatus() == ProductStatus.SOLD_OUT) {
            return;
        }
        productDomainService.validateStatusTransition(product.getStatus(), ProductStatus.SOLD_OUT);
        product.markSoldOutByOrder();
        productRepository.save(product);
    }

    private void saveOutboxEvent(String topic, Object payload) {
        try {
            outboxEventRepository.save(topic, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox 이벤트 직렬화 실패: " + topic, e);
        }
    }
}
