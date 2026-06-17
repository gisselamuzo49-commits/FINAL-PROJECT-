package com.uce.document_service.repositories;

import com.uce.document_service.models.DocumentoResumen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoResumenRepository extends MongoRepository<DocumentoResumen, String> {
}
