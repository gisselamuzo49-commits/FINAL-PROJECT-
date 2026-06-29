package com.uce.gateway_service.etcd;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EtcdConfigServiceTest {

    private Client etcdClient;
    private KV kvClient;
    private EtcdConfigService service;

    @BeforeEach
    void setUp() {
        etcdClient = mock(Client.class);
        kvClient = mock(KV.class);
        when(etcdClient.getKVClient()).thenReturn(kvClient);
        
        service = new EtcdConfigService(etcdClient);
    }

    @Test
    void testGetDatabaseInfoReturnsCorrectJson() {
        String info = service.getDatabaseInfo();

        assertNotNull(info);
        assertTrue(info.contains("\"database\": \"etcd\""));
        assertTrue(info.contains("\"type\": \"distributed-key-value\""));
        assertTrue(info.contains("\"use_case\": \"gateway-configuration\""));
        assertTrue(info.contains("\"status\": \"connected\""));
    }

    @Test
    void testSetConfigHandlesExceptionGracefully() {
        when(kvClient.put(any(), any())).thenThrow(new RuntimeException("etcd error"));

        assertDoesNotThrow(() -> {
            service.setConfig("testKey", "testValue");
        });
    }

    @Test
    void testGetConfigHandlesExceptionGracefully() {
        when(kvClient.get(any())).thenThrow(new RuntimeException("etcd error"));

        assertDoesNotThrow(() -> {
            String value = service.getConfig("testKey");
            assertNull(value);
        });
    }
}
