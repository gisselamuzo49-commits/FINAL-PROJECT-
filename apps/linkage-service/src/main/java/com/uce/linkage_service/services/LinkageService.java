package com.uce.linkage_service.services;

import com.uce.linkage_service.models.LinkageProject;
import com.uce.linkage_service.repositories.LinkageProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Collections;

@Service
public class LinkageService {

    @Autowired
    private LinkageProjectRepository linkageProjectRepository;

    public LinkageProject createProject(LinkageProject project) {
        return linkageProjectRepository.save(project);
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getLinkageFallback")
    public List<LinkageProject> getAllProjects() {
        return linkageProjectRepository.findAll();
    }

    public List<LinkageProject> getLinkageFallback(Throwable t) {
        return Collections.emptyList();
    }

    public Optional<LinkageProject> getProjectById(Long id) {
        return linkageProjectRepository.findById(id);
    }
}
