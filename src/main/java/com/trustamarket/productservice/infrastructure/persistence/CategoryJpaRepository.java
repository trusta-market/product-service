package com.trustamarket.productservice.infrastructure.persistence;

import com.trustamarket.productservice.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    @Query("select c from CategoryJpaEntity c left join fetch c.parent where c.id = :id and c.deleted = false")
    Optional<CategoryJpaEntity> findByIdWithParent(@Param("id") UUID id);

    @Query("select c from CategoryJpaEntity c left join fetch c.parent where c.deleted = false")
    List<CategoryJpaEntity> findAllWithParent();

    @Query("select c from CategoryJpaEntity c left join fetch c.parent where c.parent is null and c.deleted = false")
    List<CategoryJpaEntity> findByParentIsNullWithParent();

    @Query("select c from CategoryJpaEntity c left join fetch c.parent where c.parent.id = :parentId and c.deleted = false")
    List<CategoryJpaEntity> findByParentIdWithParent(@Param("parentId") UUID parentId);
}
