package br.com.backend.cadus.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.minio")
public record StorageProperties(
    String endpoint,
    String accessKey,
    String secretKey,
    String bucket,
    String region,
    Duration urlTtl) {}
