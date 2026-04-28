package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
    List<CategoryJpaEntity> findByParentIsNull();
    List<CategoryJpaEntity> findByParentId(UUID parentId);
}
