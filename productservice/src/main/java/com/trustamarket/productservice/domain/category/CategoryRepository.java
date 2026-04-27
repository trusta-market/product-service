package com.trustamarket.productservice.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    List<Category> findByParentIsNull();  // 최상위 카테고리만
    Category save(Category category);
}
