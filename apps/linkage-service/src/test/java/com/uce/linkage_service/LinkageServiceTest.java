package com.uce.linkage_service;

import com.uce.linkage_service.models.LinkageProject;
import com.uce.linkage_service.repositories.LinkageProjectRepository;
import com.uce.linkage_service.services.LinkageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LinkageServiceTest {

    @Mock
    private LinkageProjectRepository linkageProjectRepository;

    @InjectMocks
    private LinkageService linkageService;

    @Test
    public void test_createProject_exitoso() {
        LinkageProject project = new LinkageProject();
        project.setName("Digital Literacy");
        project.setInstitution("GAD Calderon");

        Mockito.when(linkageProjectRepository.save(project)).thenReturn(project);

        LinkageProject result = linkageService.createProject(project);
        assertNotNull(result);
        assertEquals("Digital Literacy", result.getName());
        Mockito.verify(linkageProjectRepository, Mockito.times(1)).save(project);
    }

    @Test
    public void test_createProject_nombre_vacio() {
        LinkageProject project = new LinkageProject();
        project.setName("");
        project.setInstitution("GAD Calderon");

        assertThrows(IllegalArgumentException.class, () -> {
            linkageService.createProject(project);
        });
        Mockito.verify(linkageProjectRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_getProjectById_existe() {
        LinkageProject project = new LinkageProject();
        project.setId(1L);
        project.setName("Digital Literacy");

        Mockito.when(linkageProjectRepository.findById(1L)).thenReturn(Optional.of(project));

        Optional<LinkageProject> result = linkageService.getProjectById(1L);
        assertTrue(result.isPresent());
        assertEquals("Digital Literacy", result.get().getName());
    }

    @Test
    public void test_getProjectById_no_existe() {
        Mockito.when(linkageProjectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            linkageService.getProjectById(99L);
        });
    }

    @Test
    public void test_getAllProjects_retorna_lista() {
        LinkageProject project1 = new LinkageProject();
        project1.setName("Project 1");
        LinkageProject project2 = new LinkageProject();
        project2.setName("Project 2");

        Mockito.when(linkageProjectRepository.findAll()).thenReturn(Arrays.asList(project1, project2));

        List<LinkageProject> list = linkageService.getAllProjects();
        assertEquals(2, list.size());
        assertEquals("Project 1", list.get(0).getName());
    }

    @Test
    public void test_updateProject_exitoso() {
        LinkageProject project = new LinkageProject();
        project.setId(1L);
        project.setName("Updated Name");

        Mockito.when(linkageProjectRepository.save(project)).thenReturn(project);

        LinkageProject result = linkageService.updateProject(project);
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        Mockito.verify(linkageProjectRepository, Mockito.times(1)).save(project);
    }
}
