package com.trustamarket.productservice.application.event;

import com.trustamarket.productservice.application.port.ProductSearchPort;
import com.trustamarket.productservice.application.port.ProductEventPublishPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductSearchPort productSearchPort;
    private final ProductEventPublishPort productEventPublishPort;

    // 상품 등록 후 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreated(ProductCreatedEvent event) {
        productEventPublishPort.publishProductCreated(event.getProduct());
    }

    // 상품 수정 후 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductUpdatedEvent event) {
        productSearchPort.index(event.getProduct());
    }

    // 상품 삭제 후 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        productSearchPort.delete(event.getProductId());
        productEventPublishPort.publishProductDeleted(event.getProductId());
    }

    // 검수 완료 후 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductInspected(ProductInspectedEvent event) {
        productSearchPort.index(event.getProduct());
    }

    // 검수 결과 수신 후(PRICE_SUGGESTED 전환): ES 재인덱싱
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInspectionResultReceived(InspectionResultReceivedEvent event) {
        productSearchPort.index(event.getProduct());
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
