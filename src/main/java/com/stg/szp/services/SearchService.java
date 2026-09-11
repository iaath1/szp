package com.stg.szp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;

import com.stg.szp.DTO.SearchItemDTO;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.Task;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.SZP_UserRepository;
import com.stg.szp.repos.TaskRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SearchService {
    private final ProjectRepository projectRepo;
    private final TaskRepository taskRepo;
    private final SZP_UserRepository userRepo;

    public List<SearchItemDTO> globalSearch(SZP_User user, String query) {
        List<SearchItemDTO> results = new ArrayList<>();

        if(query == null || query.trim().length() < 2) {
            return results;
        }

        List<Project> projects = projectRepo.searchProjects(user.getId(), query);
        results.addAll(projects.stream().limit(5).map(p -> SearchItemDTO.builder()
            .id(p.getId())
            .title(p.getTitle())
            .subtitle("Project key: " + p.getProjectKey())
            .type("PROJECT")
            .url("/projects/" + p.getId())
            .build()
        ).collect(Collectors.toList()));

        List<Task> tasks = taskRepo.searchTasks(user.getId(), query);
        results.addAll(tasks.stream().limit(5).map(t -> SearchItemDTO.builder()
            .id(t.getId())
            .title(t.getTitle())
            .subtitle("Project: " + t.getProject().getTitle())
            .type("TASK")
            .url("/projects/" + t.getProject().getId() + "/tasks")
            .build()
        ).collect(Collectors.toList()));

        List<SZP_User> users = userRepo.searchUsers(query);
        results.addAll(users.stream().limit(5).map(u -> SearchItemDTO.builder()
            .id(u.getId())
            .title(u.getName() + " " + u.getSurname())
            .subtitle(u.getEmail())
            .type("USER")
            .url("/user/" + u.getId())
            .build()
        ).collect(Collectors.toList()));

        return results;
    }

    
}
