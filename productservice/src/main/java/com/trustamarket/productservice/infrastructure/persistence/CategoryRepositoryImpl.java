package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.domain.category.Category;
import com.trustamarket.productservice.domain.category.CategoryRepository;
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
        return categoryJpaRepository.findById(id)
                .map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll()
                .stream()
                .map(categoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentIsNull() {
        return categoryJpaRepository.findByParentIsNull()
                .stream()
                .map(categoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return categoryJpaRepository.findByParentId(parentId)
                .stream()
                .map(categoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Category save(Category category) {
        // 도메인을 엔티티로 변환하여 저장 후 다시 도메인으로 복구
        return categoryMapper.toDomain(
                categoryJpaRepository.save(
                        categoryMapper.toJpaEntity(category)
                )
        );
    }

    @Override
    public void deleteById(UUID id) {
        categoryJpaRepository.deleteById(id);
    }
}