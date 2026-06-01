package com.uce.internship_service.repositories;

import com.uce.internship_service.models.Internship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long> {
    // Spring Boot ya sabe hacer todo (guardar, borrar, buscar) solo con esta línea
}
