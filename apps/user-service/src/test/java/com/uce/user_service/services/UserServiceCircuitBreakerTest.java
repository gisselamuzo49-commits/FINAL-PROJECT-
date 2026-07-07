package com.uce.user_service.services;

import com.uce.user_service.models.UserProfile;
import com.uce.user_service.repositories.UserProfileRepository;
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
public class UserServiceCircuitBreakerTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserProfileRepository userProfileRepository;

    @Test
    public void testGetAllProfilesFallback_WhenRepositoryThrows() {
        Mockito.when(userProfileRepository.findAll())
                .thenThrow(new RuntimeException("DB Connection Timeout"));

        List<UserProfile> results = userService.getAllProfiles();

        assertTrue(results.isEmpty());
    }
}
