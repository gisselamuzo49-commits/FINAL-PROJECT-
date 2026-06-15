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
		// Disable Kafka auto-config trying to connect to a real broker
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
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

	@Test
	void contextLoads() {
	}

}
