package com.trustamarket.productservice.application.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustamarket.productservice.application.ProductCommandService;
import com.trustamarket.productservice.application.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionCompletedListener {

    private final ProductCommandService productCommandService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${trusta.messaging.topic.inspection-completed}",
            groupId = "product-inspection-completed-group"
    )
    public void consume(String payload, Acknowledgment ack) {
        InspectionCompletedMessage message;
        try {
            message = objectMapper.readValue(payload, InspectionCompletedMessage.class);
        } catch (JsonProcessingException e) {
            log.error("[InspectionCompleted] 역직렬화 실패, skip — payload={}", payload, e);
            ack.acknowledge();
            return;
        }

        log.info("[InspectionCompleted] 수신 - productId: {}, grade: {}, suggestedPriceAmount: {}",
                message.productId(), message.grade(), message.suggestedPriceAmount());
        try {
            productCommandService.receiveInspectionResult(
                    message.productId(),
                    message.grade(),
                    message.suggestedPriceAmount(),
                    message.inspectorId()
            );
            ack.acknowledge();
            log.info("[InspectionCompleted] 처리 완료 - productId: {}", message.productId());
        } catch (ProductNotFoundException e) {
            log.warn("[InspectionCompleted] 상품 없음, skip - productId: {}", message.productId(), e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[InspectionCompleted] 처리 실패 - productId: {}", message.productId(), e);
            throw e;
        }
    }
}
