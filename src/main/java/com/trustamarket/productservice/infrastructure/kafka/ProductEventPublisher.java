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
    private static final String INSPECTION_REQUESTED_TOPIC      = "inspection.requested";
    private static final String INSPECTION_PRICE_ACCEPTED_TOPIC = "inspection.price.accepted";
    private static final String INSPECTION_PRICE_REJECTED_TOPIC = "inspection.price.rejected";


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

    // 검수 요청 이벤트 발행 (inspection-service, delivery-service가 구독)
    @Override
    public void publishInspectionRequested(UUID productId, UUID sellerId, UUID centerId, long originalPriceAmount, String currency) {
        try {
            InspectionRequestedEvent event = new InspectionRequestedEvent(
                    productId, sellerId, centerId, originalPriceAmount, currency
            );
            kafkaTemplate.send(INSPECTION_REQUESTED_TOPIC, productId.toString(), event);
            log.info("InspectionRequestedEvent 발행 완료 - productId: {}", productId);
        } catch (Exception e) {
            log.error("InspectionRequestedEvent 발행 실패 - productId: {}", productId, e);
        }
    }

    // 판매자 수락 이벤트 발행 (inspection-service가 구독 → ACCEPTED 처리)
    @Override
    public void publishInspectionPriceAccepted(Product product) {
        try {
            InspectionPriceAcceptedEvent event = new InspectionPriceAcceptedEvent(
                    product.getId(),
                    product.getSellerId(),
                    product.getPrice()  // 수락 후 확정된 최종가격
            );
            kafkaTemplate.send(INSPECTION_PRICE_ACCEPTED_TOPIC, product.getId().toString(), event);
            log.info("InspectionPriceAcceptedEvent 발행 완료 - productId: {}", product.getId());
        } catch (Exception e) {
            log.error("InspectionPriceAcceptedEvent 발행 실패 - productId: {}", product.getId(), e);
        }
    }

    // 판매자 거절 이벤트 발행 (inspection-service가 구독 → REJECTED 처리)
    @Override
    public void publishInspectionPriceRejected(Product product, String reason) {
        try {
            InspectionPriceRejectedEvent event = new InspectionPriceRejectedEvent(
                    product.getId(),
                    product.getSellerId(),
                    reason
            );
            kafkaTemplate.send(INSPECTION_PRICE_REJECTED_TOPIC, product.getId().toString(), event);
            log.info("InspectionPriceRejectedEvent 발행 완료 - productId: {}", product.getId());
        } catch (Exception e) {
            log.error("InspectionPriceRejectedEvent 발행 실패 - productId: {}", product.getId(), e);
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

    // inspection-service, delivery-service가 소비 → Inspection 생성 및 배송 생성
    public record InspectionRequestedEvent(
            UUID productId,
            UUID sellerId,
            UUID centerId,
            long originalPriceAmount,
            String currency
    ) {}

    // inspection-service가 소비 → inspection.acceptPrice() 호출
    public record InspectionPriceAcceptedEvent(
            UUID productId,
            UUID sellerId,
            Long finalPrice
    ) {}

    // inspection-service가 소비 → inspection.rejectPrice() 호출
    public record InspectionPriceRejectedEvent(
            UUID productId,
            UUID sellerId,
            String reason
    ) {}
}
