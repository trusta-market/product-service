package com.trustamarket.productservice.domain.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    List<Category> findByParentIsNull();
    List<Category> findByParentId(UUID parentId);
    Category save(Category category);
    void deleteById(UUID id);
}