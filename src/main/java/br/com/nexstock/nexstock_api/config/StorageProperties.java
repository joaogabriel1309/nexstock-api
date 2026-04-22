package br.com.nexstock.nexstock_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.List;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        DataSize maxFileSize,
        List<String> allowedContentTypes,
        S3 s3
) {
    public record S3(
            String bucket,
            String region,
            String publicBaseUrl,
            String productImagesPrefix
    ) {
    }
}
