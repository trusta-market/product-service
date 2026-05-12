package com.trustamarket.productservice.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class InspectionRequestDto {

    @NotNull(message = "centerId는 필수입니다.")
    private UUID centerId;
}
