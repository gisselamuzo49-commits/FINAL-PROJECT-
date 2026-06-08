package com.uce.linkage_service.services;

import com.uce.linkage_service.models.LinkageProject;
import com.uce.linkage_service.repositories.LinkageProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinkageService {

    @Autowired
    private LinkageProjectRepository linkageProjectRepository;

    public LinkageProject createProject(LinkageProject project) {
        return linkageProjectRepository.save(project);
    }

    public List<LinkageProject> getAllProjects() {
        return linkageProjectRepository.findAll();
    }

    public LinkageProject getProjectById(Long id) {
        return linkageProjectRepository.findById(id).orElse(null);
    }
}
