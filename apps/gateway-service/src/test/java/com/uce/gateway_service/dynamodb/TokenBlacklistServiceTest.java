package com.uce.gateway_service.dynamodb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TokenBlacklistServiceTest {

    private DynamoDbClient dynamoDbClient;
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        tokenBlacklistService = new TokenBlacklistService(dynamoDbClient);
    }

    @Test
    void testRevokeTokenCallsPutItem() {
        String tokenHash = "test-token-hash";
        long ttlSeconds = 3600;

        when(dynamoDbClient.putItem(any(Consumer.class))).thenReturn(PutItemResponse.builder().build());

        tokenBlacklistService.revokeToken(tokenHash, ttlSeconds);

        verify(dynamoDbClient, times(1)).putItem(any(Consumer.class));
    }

    @Test
    void testIsTokenRevokedReturnsTrueWhenExists() {
        String tokenHash = "revoked-token-hash";

        GetItemResponse mockResponse = GetItemResponse.builder()
            .item(Map.of("token_hash", AttributeValue.builder().s(tokenHash).build()))
            .build();

        when(dynamoDbClient.getItem(any(Consumer.class))).thenReturn(mockResponse);

        boolean isRevoked = tokenBlacklistService.isTokenRevoked(tokenHash);

        assertTrue(isRevoked);
        verify(dynamoDbClient, times(1)).getItem(any(Consumer.class));
    }

    @Test
    void testIsTokenRevokedReturnsFalseWhenNotExists() {
        String tokenHash = "active-token-hash";

        GetItemResponse mockResponse = GetItemResponse.builder()
            .item(null)
            .build();

        when(dynamoDbClient.getItem(any(Consumer.class))).thenReturn(mockResponse);

        boolean isRevoked = tokenBlacklistService.isTokenRevoked(tokenHash);

        assertFalse(isRevoked);
        verify(dynamoDbClient, times(1)).getItem(any(Consumer.class));
    }
}
