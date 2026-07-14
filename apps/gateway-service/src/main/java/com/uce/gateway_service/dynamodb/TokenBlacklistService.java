package com.uce.gateway_service.dynamodb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "jwt-blacklist";
    
    @org.springframework.beans.factory.annotation.Autowired
    public TokenBlacklistService(@Value("${aws.region:us-east-1}") String region) {
        this.dynamoDbClient = DynamoDbClient.builder()
            .region(Region.of(region))
            .build();
        ensureTableExists();
    }

    // Constructor para pruebas unitarias
    TokenBlacklistService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }
    
    private void ensureTableExists() {
        try {
            dynamoDbClient.describeTable(r -> r.tableName(tableName));
        } catch (ResourceNotFoundException e) {
            try {
                dynamoDbClient.createTable(r -> r
                    .tableName(tableName)
                    .keySchema(KeySchemaElement.builder()
                        .attributeName("token_hash")
                        .keyType(KeyType.HASH)
                        .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("token_hash")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                );
            } catch (Exception ex) {
                logger.warn("Unable to create DynamoDB token blacklist table; revocation features may be unavailable", ex);
            }
        } catch (Exception e) {
            logger.warn("Unable to verify DynamoDB token blacklist table; revocation features may be unavailable", e);
        }
    }
    
    public void revokeToken(String tokenHash, long ttlSeconds) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("token_hash", AttributeValue.builder().s(tokenHash).build());
        item.put("revoked_at", AttributeValue.builder().s(Instant.now().toString()).build());
        item.put("ttl", AttributeValue.builder().n(
            String.valueOf(Instant.now().plusSeconds(ttlSeconds).getEpochSecond())
        ).build());
        
        dynamoDbClient.putItem(r -> r.tableName(tableName).item(item));
    }
    
    public boolean isTokenRevoked(String tokenHash) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("token_hash", AttributeValue.builder().s(tokenHash).build());
            
            GetItemResponse response = dynamoDbClient.getItem(r -> r
                .tableName(tableName)
                .key(key)
            );
            return response.hasItem();
        } catch (Exception e) {
            logger.error("Unable to check whether token is revoked", e);
            throw new IllegalStateException("Token revocation status could not be verified", e);
        }
    }
}
