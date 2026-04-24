package com.trustamarket.productservice.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "p_Products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {

    private static final int MAX_IMAGE_COUNT = 10;
    private static final int MAX_TITLE_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    private ProductGrade grade;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    private InspectionStatus inspectionStatus;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    private List<ProductImage> images;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Product(Long sellerId, Long categoryId, String title,
                    String description, int price, ProductGrade grade, boolean requiresInspection) {
        validate(title, price);
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.grade = grade;
        this.status = requiresInspection
                ? ProductStatus.PENDING_INSPECTION
                : ProductStatus.ON_SALE;
        this.inspectionStatus = requiresInspection
                ? InspectionStatus.PENDING
                : InspectionStatus.NONE;
        this.images = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    // 상품등록
    public static Product create(Long sellerId, Long categoryId, String title,
                                 String description, int price, ProductGrade grade, boolean requiresInspection) {
        return new Product(sellerId, categoryId, title, description, price, grade, requiresInspection);
    }

    // db에 저자오디어있던 id나 등록시간 같은걸 다시 살려낸다
    public static Product restore(Long id, Long sellerId, Long categoryId, String title,
                                  String description, int price, ProductGrade grade,
                                  ProductStatus status,InspectionStatus inspectionStatus, List<ProductImage> images,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        Product product = new Product();
        product.id = id;
        product.sellerId = sellerId;
        product.categoryId = categoryId;
        product.title = title;
        product.description = description;
        product.price = price;
        product.grade = grade;
        product.status = status;
        product.inspectionStatus = inspectionStatus;
        product.images = (images != null) ? new ArrayList<>(images) : new ArrayList<>();
        product.createdAt = createdAt;
        product.updatedAt = updatedAt;
        return product;
    }


    // 제목, 가격 같은 상세내용 수정
    public void update(String title, String description, int price,
                       ProductGrade grade, Long categoryId) {
        validate(title, price);
        this.title = title;
        this.description = description;
        this.price = price;
        this.grade = grade;
        this.categoryId = categoryId;
        onUpdate();
    }

    // 예약중 상태
    public void reserve() {
        if (!this.status.isAvailableForOrder()) {
            throw new IllegalStateException("판매중인 상품만 예약할 수 있습니다. 현재 상태: " + this.status.getDescription());
        }
        this.status = ProductStatus.RESERVED;
        onUpdate();
    }

    // 판매완료
    public void completeSale() {
        if (this.status != ProductStatus.RESERVED) {
            throw new IllegalStateException("예약중인 상품만 판매완료 처리할 수 있습니다.");
        }
        this.status = ProductStatus.SOLD_OUT;
        onUpdate();
    }

    // 예약취소로 다시 판매중으로 수정
    public void cancelReservation() {
        if (this.status != ProductStatus.RESERVED) {
            throw new IllegalStateException("예약중인 상품만 예약 취소할 수 있습니다.");
        }
        this.status = ProductStatus.ON_SALE;
        onUpdate();
    }

    // 검수 시작 (검수자가 상품 수령 후)
    public void startInspection() {
        if (this.inspectionStatus != InspectionStatus.PENDING) {
            throw new IllegalStateException(
                    "검수 대기 상태인 상품만 검수를 시작할 수 있습니다. 현재 상태: " + this.inspectionStatus.getDescription());
        }
        this.inspectionStatus = InspectionStatus.IN_PROGRESS;
        onUpdate();
    }

    // 검수 대상 여부 — 카테고리별 기준 금액과 비교
    public boolean requiresInspection(int highValueThreshold) {
        return this.price >= highValueThreshold;
    }

    // 검수 통과 → 등급 확정 + 상세페이지 검수완료 뱃지 표시
    public void completeInspection(ProductGrade inspectedGrade) {
        if (this.inspectionStatus != InspectionStatus.IN_PROGRESS) {
            throw new IllegalStateException("검수 중인 상품만 등급을 확정할 수 있습니다.");
        }
        this.grade = inspectedGrade;
        this.inspectionStatus = InspectionStatus.PASSED;
        this.status = ProductStatus.ON_SALE;
        onUpdate();
    }

    // 검수 불합격 → 판매자에게 반송
    public void failInspection() {
        if (this.inspectionStatus != InspectionStatus.IN_PROGRESS) {
            throw new IllegalStateException("검수 중인 상품만 검수 불합격 처리할 수 있습니다.");
        }
        this.inspectionStatus = InspectionStatus.FAILED;
        onUpdate();
    }

    // 상품 이미지 추가
    public void addImage(ProductImage image) {
        if (this.images.size() >= MAX_IMAGE_COUNT) {
            throw new IllegalStateException("이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 등록 가능합니다.");
        }

        if (shouldSetAsThumbnail()) {
            image.markAsThumbnail();
        }

        this.images.add(image);
        onUpdate();
    }

    // 이미지 삭제
    public void removeImage(Long imageId) {
        boolean removed = this.images.removeIf(img -> Objects.equals(img.getId(), imageId));
        if (!removed) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다. imageId: " + imageId);
        }

        if (!this.images.isEmpty() && getThumbnail() == null) {
            this.images.get(0).markAsThumbnail();
        }

        onUpdate();
    }

    // 사진 순서 수정
    public void reorderImages(List<Long> imageIdsSortedOrder) {
        for (int i = 0; i < imageIdsSortedOrder.size(); i++) {
            Long imageId = imageIdsSortedOrder.get(i);
            int order = i;
            this.images.stream()
                    .filter(img -> Objects.equals(img.getId(), imageId))
                    .findFirst()
                    .ifPresent(img -> img.changeSortOrder(order));
        }
        onUpdate();
    }

    // 상품주인 확인
    public boolean isOwnedBy(Long sellerId) {
        return Objects.equals(this.sellerId, sellerId);
    }

    // 상품 상태 확인
    public boolean isSaleable() {
        return this.status == ProductStatus.ON_SALE;
    }

    // 상세페이지 검수완료 뱃지 표시 여부
    public boolean isInspectionVerified() {
        return this.inspectionStatus.isVerified();
    }

    // 대표이미지 확인
    public ProductImage getThumbnail() {
        return this.images.stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .orElse(this.images.isEmpty() ? null : this.images.get(0));
    }

    // 대표이미지 취소 및 재설정
    public void changeThumbnail(Long newThumbnailImageId) {
        images.stream()
                .filter(ProductImage::isThumbnail)
                .forEach(ProductImage::unmarkThumbnail);
        images.stream()
                .filter(img -> Objects.equals(img.getId(), newThumbnailImageId))
                .findFirst()
                .ifPresentOrElse(
                        ProductImage::markAsThumbnail,
                        () -> { throw new IllegalArgumentException("해당 ID를 가진 이미지가 상품에 존재하지 않습니다."); }
                );
        onUpdate();
    }

    //외부에서 조회시 이미지를 읽기전용으로
    public List<ProductImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    // 첫번째 사진을 대표이미지로 설정
    private boolean shouldSetAsThumbnail() {
        return this.images.isEmpty() || this.images.stream().noneMatch(ProductImage::isThumbnail);
    }

    // 상품정보 수정시 완료시간 확인
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 상품정보 확인
    private void validate(String title, int price) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("상품명은 " + MAX_TITLE_LENGTH + "자 이내여야 합니다.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }
    }
}