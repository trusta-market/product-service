package com.trustamarket.productservice.application;

import com.trustamarket.productservice.application.exception.CategoryNotFoundException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.CategoryRepository;
import com.trustamarket.productservice.domain.category.InspectionPolicy;
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

    public List<Category> findAll() { return categoryRepository.findAll(); }
    public List<Category> findRootCategories() { return categoryRepository.findByParentIsNull(); }
    public List<Category> findSubCategories(UUID parentId) { return categoryRepository.findByParentId(parentId); }

    public Category findById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    public Category create(String name, UUID parentId, int depth, int displayOrder,
                           Integer inspectionThreshold, InspectionPolicy inspectionPolicy) {
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
                        .inspectionThreshold(inspectionThreshold)
                        .inspectionPolicy(inspectionPolicy)
                        .build()
        );
    }

    @Transactional
    public void delete(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(ProductErrorCode.CATEGORY_NOT_FOUND));
        category.softDelete();
        categoryRepository.save(category);
    }
}