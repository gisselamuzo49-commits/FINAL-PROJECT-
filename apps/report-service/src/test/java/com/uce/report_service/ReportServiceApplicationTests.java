package com.uce.report_service;

import com.uce.report_service.repositories.ReporteGlobalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:report_test_db;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=update"
})
class ReportServiceApplicationTests {

    @MockitoBean
    private ReporteGlobalRepository globalRepository;

    @Test
    void contextLoads() {
    }
}
