package com.trustamarket.productservice.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageReorderRequest {

    @NotEmpty(message = "이미지 순서 목록은 필수입니다.")
    @Size(max = 10, message = "이미지는 최대 10개까지 순서 변경이 가능합니다.") // 정책에 맞게 조절
    private List<UUID> imageIds;
}