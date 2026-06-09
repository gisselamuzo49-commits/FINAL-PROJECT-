package com.uce.linkage_service.repositories;

import com.uce.linkage_service.models.LinkageProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkageProjectRepository extends JpaRepository<LinkageProject, Long> {
}
