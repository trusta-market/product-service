package com.trustamarket.productservice.application.event;

import com.trustamarket.productservice.application.port.ProductSearchPort;
import com.trustamarket.productservice.application.port.ProductEventPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
}
