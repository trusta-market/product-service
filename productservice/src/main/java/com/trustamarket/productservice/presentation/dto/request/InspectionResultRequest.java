package com.trustamarket.productservice.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class InspectionResultRequest {

    @NotNull(message = "수락 여부는 필수입니다.")
    private Boolean accepted;

    // 거절 시 사유 (accepted=false 일 때 필수)
    private String reason;
}