package com.uce.gateway_service.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@Service
public class EtcdConfigService {

    private static final Logger logger = LoggerFactory.getLogger(EtcdConfigService.class);

    private final Client etcdClient;

    @org.springframework.beans.factory.annotation.Autowired
    public EtcdConfigService(
        @Value("${etcd.endpoints}") String endpoints
    ) {
        this.etcdClient = Client.builder()
            .endpoints(endpoints)
            .build();
    }

    // Constructor para pruebas unitarias
    EtcdConfigService(Client etcdClient) {
        this.etcdClient = etcdClient;
    }

    public void setConfig(String key, String value) {
        try {
            ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
            ByteSequence bsValue = ByteSequence.from(value, StandardCharsets.UTF_8);
            etcdClient.getKVClient().put(bsKey, bsValue).get();
        } catch (Exception e) {
            logger.error("Unable to write etcd configuration for key {}", key, e);
        }
    }

    public String getConfig(String key) {
        try {
            ByteSequence bsKey = ByteSequence.from(key, StandardCharsets.UTF_8);
            GetResponse response = etcdClient.getKVClient()
                .get(bsKey).get();
            if (!response.getKvs().isEmpty()) {
                return response.getKvs().get(0).getValue()
                    .toString(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.error("Unable to read etcd configuration for key {}", key, e);
        }
        return null;
    }

    public String getDatabaseInfo() {
        return "{\"database\": \"etcd\", " +
               "\"type\": \"distributed-key-value\", " +
               "\"use_case\": \"gateway-configuration\", " +
               "\"status\": \"connected\"}";
    }
}
