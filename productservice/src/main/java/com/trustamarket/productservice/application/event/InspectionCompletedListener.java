package com.trustamarket.productservice.application.event;

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

    @KafkaListener(
            topics = "${trusta.messaging.topic.inspection-completed}",
            groupId = "product-inspection-completed-group",
            containerFactory = "inspectionCompletedListenerContainerFactory"
    )
    public void consume(InspectionCompletedMessage message, Acknowledgment ack) {
        log.info("[InspectionCompleted] 수신 - productId: {}, grade: {}, suggestedPrice: {}",
                message.productId(), message.grade(), message.suggestedPrice());
        try {
            productCommandService.receiveInspectionResult(
                    message.productId(),
                    message.grade(),
                    message.suggestedPrice(),
                    message.inspectorId()
            );
            ack.acknowledge();
            log.info("[InspectionCompleted] 처리 완료 - productId: {} → PRICE_SUGGESTED", message.productId());
        } catch (ProductNotFoundException e) {
            // 상품이 없는 경우 — 재시도 불필요, ack 후 skip
            log.warn("[InspectionCompleted] 상품 없음, skip - productId: {}", message.productId(), e);
            ack.acknowledge();
        } catch (Exception e) {
            // 일시적 오류 — ack 하지 않아 재시도 가능
            log.error("[InspectionCompleted] 처리 실패 - productId: {}", message.productId(), e);
            throw e;
        }
    }
}
