package com.uce.document_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    @Value("${aws.sessionToken:}")
    private String sessionToken;

    @Bean
    @ConditionalOnProperty(name = "s3.bucket.name")
    public S3Client s3Client() {
        if (sessionToken != null && !sessionToken.trim().isEmpty()) {
            return S3Client.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
                    ))
                    .build();
        } else if (accessKeyId != null && !accessKeyId.trim().isEmpty() &&
                   secretAccessKey != null && !secretAccessKey.trim().isEmpty()) {
            return S3Client.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    ))
                    .build();
        } else {
            // Fallback to default credential provider chain
            return S3Client.builder()
                    .region(Region.US_EAST_1)
                    .build();
        }
    }
}
