package com.uce.linkage_service.services;

import com.uce.linkage_service.models.LinkageProject;
import com.uce.linkage_service.repositories.LinkageProjectRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class LinkageServiceCircuitBreakerTest {

    @Autowired
    private LinkageService linkageService;

    @MockitoBean
    private LinkageProjectRepository linkageProjectRepository;

    @Test
    public void testGetAllProjectsFallback_WhenRepositoryThrows() {
        Mockito.when(linkageProjectRepository.findAll())
                .thenThrow(new RuntimeException("DB Connection Timeout"));

        List<LinkageProject> results = linkageService.getAllProjects();

        assertTrue(results.isEmpty());
    }
}
