package com.trustamarket.productservice.presentation.controller;

import com.trustamarket.productservice.application.CategoryService;
import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.presentation.dto.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 전체 카테고리 조회
    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll()
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    // 최상위 카테고리만 조회
    @GetMapping("/root")
    public List<CategoryResponse> findRootCategories() {
        return categoryService.findRootCategories()
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    // 하위 카테고리 조회
    @GetMapping("/{parentId}/sub")
    public List<CategoryResponse> findSubCategories(@PathVariable UUID parentId) {
        return categoryService.findSubCategories(parentId)
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    // 카테고리 단건 조회
    @GetMapping("/{categoryId}")
    public CategoryResponse findById(@PathVariable UUID categoryId) {
        return CategoryResponse.from(categoryService.findById(categoryId));
    }

    // 카테고리 등록 (관리자용)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
            @RequestParam String name,
            @RequestParam(required = false) UUID parentId,
            @RequestParam int depth,
            @RequestParam int displayOrder,
            @RequestParam int highValueThreshold
    ) {
        Category category = categoryService.create(name, parentId, depth, displayOrder, highValueThreshold);
        return CategoryResponse.from(category);
    }

    // 카테고리 삭제 (관리자용)
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID categoryId) {
        categoryService.delete(categoryId);
    }
}
