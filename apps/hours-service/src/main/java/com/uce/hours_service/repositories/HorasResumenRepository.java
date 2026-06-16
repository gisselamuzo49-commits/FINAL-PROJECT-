package com.uce.hours_service.repositories;

import com.uce.hours_service.models.HorasResumen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorasResumenRepository extends MongoRepository<HorasResumen, String> {
}
