package com.trustamarket.productservice.application.event;

import com.trustamarket.productservice.application.port.ProductEventPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductEventPublishPort productEventPublishPort;

    // 상품 등록 후 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreated(ProductCreatedEvent event) {
        productEventPublishPort.publishProductCreated(event.getProduct());
    }

    // 상품 삭제 후 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        productEventPublishPort.publishProductDeleted(event.getProductId());
    }

    // 판매자 수락 후: Kafka inspection.price.accepted 발행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInspectionAccepted(InspectionAcceptedEvent event) {
        productEventPublishPort.publishInspectionPriceAccepted(event.getProduct());
    }

    // 판매자 거절 후: Kafka inspection.price.rejected 발행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInspectionRejected(InspectionRejectedEvent event) {
        productEventPublishPort.publishInspectionPriceRejected(event.getProduct(), event.getReason());
    }
}
