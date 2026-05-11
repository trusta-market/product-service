package com.trustamarket.productservice.application.event;

import com.trustamarket.productservice.application.ProductCommandService;
import com.trustamarket.productservice.application.exception.InvalidStatusTransitionException;
import com.trustamarket.productservice.application.exception.ProductNotFoundException;
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
        } catch (ProductNotFoundException | InvalidStatusTransitionException e) {
            // 비재시도성 비즈니스 실패 — 무한 재배달 방지 위해 ack + skip. (DLT 도입 전 임시)
            log.warn("[ProductSoldOut] non-retryable, ack and skip — eventId={}, productId={}",
                    message.eventId(), message.productId(), e);
            ack.acknowledge();
        } catch (Exception e) {
            // 통신/일시 오류 — ack 안 함 → 재시도 가능
            log.error("[ProductSoldOut] 처리 실패 — eventId={}, productId={}",
                    message.eventId(), message.productId(), e);
            throw e;
        }
    }
}
