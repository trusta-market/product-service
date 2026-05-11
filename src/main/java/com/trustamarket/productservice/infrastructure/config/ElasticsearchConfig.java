package com.trustamarket.productservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.trustamarket.productservice.infrastructure.elasticsearch"
)
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    // "http://localhost:9200" 형태로 들어옴
    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUri;

    @Override
    public ClientConfiguration clientConfiguration() {
        // ES 클라이언트는 "http://" 없이 "localhost:9200" 형태를 원함
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUri.replace("http://", ""))
                .build();
    }
}
