package com.uce.hours_service.repositories;

import com.uce.hours_service.models.RegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroHorasRepository extends JpaRepository<RegistroHoras, Long> {
}
