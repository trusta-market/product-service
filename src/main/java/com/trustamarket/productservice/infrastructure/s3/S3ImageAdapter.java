package com.trustamarket.productservice.infrastructure.s3;

import com.trustamarket.productservice.application.exception.InvalidImageUrlException;
import com.trustamarket.productservice.application.exception.errorcode.ProductErrorCode;
import com.trustamarket.productservice.application.port.ProductImagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageAdapter implements ProductImagePort {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 허용 확장자 목록
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "webp");

    // 이미지 업로드 → S3 저장 후 URL 반환
    @Override
    public String upload(MultipartFile file, String directory) {
        validateExtension(file.getOriginalFilename());

        String key = buildKey(directory, file.getOriginalFilename());

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
            log.info("S3 업로드 완료 - key: {}", key);
        } catch (IOException e) {
            log.error("S3 업로드 실패 - key: {}", key, e);
            throw new InvalidImageUrlException(ProductErrorCode.INVALID_IMAGE_URL);
        }

        return buildUrl(key);
    }

    // 이미지 삭제 → S3에서 제거
    @Override
    public void delete(String imageUrl) {
        String key = extractKey(imageUrl);
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            log.info("S3 삭제 완료 - key: {}", key);
        } catch (Exception e) {
            // 삭제 실패는 치명적 오류가 아니므로 로그만 기록
            log.error("S3 삭제 실패 - key: {}", key, e);
        }
    }

    // 확장자 검증
    private void validateExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new InvalidImageUrlException(ProductErrorCode.INVALID_IMAGE_URL);
        }
        String extension = filename
                .substring(filename.lastIndexOf(".") + 1)
                .toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidImageUrlException(ProductErrorCode.INVALID_IMAGE_URL);
        }
    }

    // S3 저장 경로 생성 — UUID로 파일명 중복 방지
    // 결과 예시: products/550e8400-uuid.jpg
    private String buildKey(String directory, String originalFilename) {
        String extension = originalFilename
                .substring(originalFilename.lastIndexOf(".") + 1)
                .toLowerCase();
        return directory + "/" + UUID.randomUUID() + "." + extension;
    }

    // S3 공개 URL 생성
    // 결과 예시: https://bucket-name.s3.amazonaws.com/products/uuid.jpg
    private String buildUrl(String key) {
        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }

    // URL에서 S3 key 추출
    // https://bucket.s3.amazonaws.com/products/uuid.jpg → products/uuid.jpg
    private String extractKey(String imageUrl) {
        // URL의 전체 구조에서 버킷 주소 이후의 경로만 추출
        String baseUrl = ".amazonaws.com/";
        int index = imageUrl.indexOf(baseUrl);
        if (index == -1) {
            throw new InvalidImageUrlException(ProductErrorCode.INVALID_IMAGE_URL); // 잘못된 URL 형식 대응
        }
        return imageUrl.substring(index + baseUrl.length());
    }
}