package com.trustamarket.productservice.application.event;

import com.trustamarket.productservice.domain.product.ProductGrade;

import java.util.UUID;

public record InspectionCompletedMessage(
        UUID productId,
        UUID inspectorId,
        ProductGrade grade,
        Long suggestedPriceAmount
) {
}
