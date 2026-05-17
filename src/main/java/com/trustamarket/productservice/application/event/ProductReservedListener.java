package com.trustamarket.productservice.application.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustamarket.productservice.application.ProductCommandService;
import com.trustamarket.productservice.application.exception.InvalidStatusTransitionException;
import com.trustamarket.productservice.application.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductReservedListener {

    private final ProductCommandService productCommandService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${trusta.messaging.topic.order-paid}",  // product-reserved → order-paid
            groupId = "product-order-paid-group"
    )
    public void consume(String payload, Acknowledgment ack) {
        ProductReservedMessage message;
        try {
            message = objectMapper.readValue(payload, ProductReservedMessage.class);
        } catch (JsonProcessingException e) {
            log.error("[ProductReserved] 역직렬화 실패, skip — payload={}", payload, e);
            ack.acknowledge();
            return;
        }

        try {
            log.info("[ProductReserved] consume — eventId={}, productId={}, orderId={}",
                    message.eventId(), message.productId(), message.orderId());
            productCommandService.reserveByOrder(message.productId());
            ack.acknowledge();
        } catch (ProductNotFoundException | InvalidStatusTransitionException e) {
            log.warn("[ProductReserved] non-retryable, ack and skip — eventId={}, productId={}",
                    message.eventId(), message.productId(), e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[ProductReserved] 처리 실패 — eventId={}, productId={}",
                    message.eventId(), message.productId(), e);
            throw e;
        }
    }
}
