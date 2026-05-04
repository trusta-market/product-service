package com.trustamarket.productservice.application.event;

import com.trustamarket.productservice.application.ProductCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

// order.product.sold-out 토픽 consume → product status SOLD_OUT 으로 전이.
// 멱등성: ProductCommandService.markSoldOutByOrder 가 이미 SOLD_OUT 인 상품은 no-op 처리.
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSoldOutListener {

    private final ProductCommandService productCommandService;

    @KafkaListener(topics = "${trusta.messaging.topic.product-sold-out}", groupId = "product-sold-out-group")
    public void consume(ProductSoldOutMessage message, Acknowledgment ack) {
        try {
            log.info("[ProductSoldOut] consume — eventId={}, productId={}, orderId={}",
                    message.eventId(), message.productId(), message.orderId());
            productCommandService.markSoldOutByOrder(message.productId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ProductSoldOut] 처리 실패 — eventId={}, productId={}",
                    message.eventId(), message.productId(), e);
            // ack 안 함 → 재시도 가능
            throw e;
        }
    }
}
