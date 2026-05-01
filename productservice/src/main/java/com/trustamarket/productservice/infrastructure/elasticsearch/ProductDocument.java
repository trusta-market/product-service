package com.trustamarket.productservice.infrastructure.elasticsearch;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

// DB의 @Entity처럼 ES 인덱스와 매핑되는 클래스
// indexName: ES에서 이 데이터를 저장할 인덱스 이름
@Getter
@Builder
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id;  // ES는 id를 String으로 사용

    // Text: 형태소 분석 후 검색 가능 (제목 검색)
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    // Integer: 숫자 범위 검색 가능 (가격 필터)
    @Field(type = FieldType.Integer)
    private int price;

    // Keyword: 정확히 일치하는 값만 검색 (상태, ID 등)
    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String grade;           // 검수 전 null 가능

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Keyword)
    private String sellerId;

    @Field(type = FieldType.Keyword)
    private String inspectionStatus;
}