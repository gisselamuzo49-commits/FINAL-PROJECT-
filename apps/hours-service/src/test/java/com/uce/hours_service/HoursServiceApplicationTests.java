package com.uce.hours_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Verifies the full Spring context boots correctly with H2 (in-memory) and
 * a mocked KafkaTemplate — no Kafka broker needed for this smoke test.
 */
@SpringBootTest(
	classes = HoursServiceApplication.class,
	properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		// Disable Kafka and Mongo auto-config trying to connect to real brokers/servers
		"spring.autoconfigure.exclude=" +
			"org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
			"org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
			"org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
	}
)
class HoursServiceApplicationTests {

	/**
	 * KafkaTemplate is excluded from auto-config above, so we provide a mock
	 * so that HoursService can still be wired correctly.
	 */
	@MockitoBean
	@SuppressWarnings("rawtypes")
	private KafkaTemplate kafkaTemplate;

	@MockitoBean
	private com.uce.hours_service.repositories.HorasResumenRepository resumenRepository;

	@MockitoBean
	private com.uce.hours_service.client.UserServiceClient userServiceClient;

	@Test
	void contextLoads() {
	}

}
