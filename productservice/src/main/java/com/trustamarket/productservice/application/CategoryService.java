package com.trustamarket.productservice.application;

import com.trustamarket.productservice.application.exception.CategoryNotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 전체 카테고리 조회
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    // 최상위 카테고리만 조회
    public List<Category> findRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    // 하위 카테고리 목록 조회
    public List<Category> findSubCategories(UUID parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    // 단건 조회
    public Category findById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }

    // 카테고리 등록 (관리자용)
    @Transactional
    public Category create(String name, UUID parentId, int depth,
                           int displayOrder, int highValueThreshold) {
        Category parent = parentId != null
                ? categoryRepository.findById(parentId)
                  .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND))
                : null;

        return categoryRepository.save(
                Category.builder()
                        .name(name)
                        .parent(parent)
                        .depth(depth)
                        .displayOrder(displayOrder)
                        .highValueThreshold(highValueThreshold)
                        .build()
        );
    }

    // 카테고리 삭제 (관리자용)
    @Transactional
    public void delete(UUID categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.deleteById(categoryId);
    }
}