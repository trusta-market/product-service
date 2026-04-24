package com.trustamarket.productservice.domain.category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(Long id);
    List<Category> findAll();
    List<Category> findByParentIsNull();  // 최상위 카테고리만
    Category save(Category category);
}
