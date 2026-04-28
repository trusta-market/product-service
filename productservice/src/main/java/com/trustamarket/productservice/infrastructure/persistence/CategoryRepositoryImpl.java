package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.CategoryRepository;
import com.trustamarket.productservice.infrastructure.persistence.entity.CategoryJpaEntity;
import com.trustamarket.productservice.infrastructure.persistence.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryJpaRepository.findByIdWithParent(id)
                .map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAllWithParent()
                .stream()
                .map(categoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentIsNull() {
        return categoryJpaRepository.findByParentIsNullWithParent()
                .stream()
                .map(categoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return categoryJpaRepository.findByParentIdWithParent(parentId)
                .stream()
                .map(categoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = categoryMapper.toJpaEntity(category);
        CategoryJpaEntity savedEntity = categoryJpaRepository.save(entity);

        return categoryMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        categoryJpaRepository.deleteById(id);
    }
}