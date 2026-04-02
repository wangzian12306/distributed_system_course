package com.example.distributed.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.distributed.system.repository")
public class ElasticSearchConfig {
    // Spring Boot 2.7+ 版本会自动配置Elasticsearch客户端，无需手动创建
    // 如需自定义配置，可以在application.yml中设置
}