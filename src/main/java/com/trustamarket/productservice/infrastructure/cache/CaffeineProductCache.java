package com.trustamarket.productservice.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.trustamarket.productservice.domain.product.Product;
import com.trustamarket.productservice.domain.product.ProductCachePort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * ProductCachePort 의 Caffeine (in-JVM) 구현.
 * pod 별 독립 cache — write 적은 read 부담 큰 endpoint 에 적합.
 * 나중에 분산 cache 필요 시 RedisProductCache 추가 + @Primary 또는 @Profile 로 교체.
 */
@Component
public class CaffeineProductCache implements ProductCachePort {

    private static final String LATEST_KEY = "latest";

    // /api/products/latest 부하 ↑ → TTL 30s (staleness 허용), maxSize 작게 (key 1개라 100이면 충분).
    private final Cache<String, List<Product>> cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();

    @Override
    public Optional<List<Product>> getLatest() {
        return Optional.ofNullable(cache.getIfPresent(LATEST_KEY));
    }

    @Override
    public void putLatest(List<Product> products) {
        cache.put(LATEST_KEY, products);
    }

    @Override
    public void evictLatest() {
        cache.invalidate(LATEST_KEY);
    }
}
