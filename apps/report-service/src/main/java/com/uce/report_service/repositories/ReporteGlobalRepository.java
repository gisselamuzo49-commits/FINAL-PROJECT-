package com.uce.report_service.repositories;

import com.uce.report_service.models.ReporteGlobal;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReporteGlobalRepository extends MongoRepository<ReporteGlobal, String> {
}
