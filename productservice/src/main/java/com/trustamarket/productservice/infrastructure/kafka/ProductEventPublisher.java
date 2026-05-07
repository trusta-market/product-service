package com.trustamarket.productservice.infrastructure.kafka;

import com.trustamarket.productservice.application.port.ProductEventPublishPort;
import com.trustamarket.productservice.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher implements ProductEventPublishPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 다른 서비스가 구독할 토픽명
    private static final String PRODUCT_CREATED_TOPIC = "product.created";
    private static final String PRODUCT_DELETED_TOPIC  = "product.deleted";

    // 상품 등록 이벤트 발행
    // Kafka 실패해도 상품 등록에 영향 없도록 예외 처리
    @Override
    public void publishProductCreated(Product product) {
        try {
            ProductCreatedEvent event = new ProductCreatedEvent(
                    product.getId(),
                    product.getSellerId(),
                    product.getCategoryId(),
                    product.getPrice(),
                    product.getInspectionStatus().name()
            );
            // send(토픽명, 메시지 key, 메시지 value)
            // key로 productId를 사용 → 같은 상품 이벤트는 항상 같은 파티션으로
            kafkaTemplate.send(
                    PRODUCT_CREATED_TOPIC,
                    product.getId().toString(),
                    event
            );
            log.info("ProductCreatedEvent 발행 완료 - productId: {}", product.getId());
        } catch (Exception e) {
            log.error("ProductCreatedEvent 발행 실패 - productId: {}", product.getId(), e);
        }
    }

    // 상품 삭제 이벤트 발행
    @Override
    public void publishProductDeleted(UUID productId) {
        try {
            kafkaTemplate.send(
                    PRODUCT_DELETED_TOPIC,
                    productId.toString(),
                    productId.toString()
            );
            log.info("ProductDeletedEvent 발행 완료 - productId: {}", productId);
        } catch (Exception e) {
            log.error("ProductDeletedEvent 발행 실패 - productId: {}", productId, e);
        }
    }

    // Kafka로 보낼 이벤트 데이터 구조
    // record: Java 16+의 불변 데이터 클래스
    public record ProductCreatedEvent(
            UUID productId,
            UUID sellerId,
            UUID categoryId,
            Long price,
            String inspectionStatus
    ) {}
}
