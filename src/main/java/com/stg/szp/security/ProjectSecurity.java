package com.stg.szp.security;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.stg.szp.models.Project;
import com.stg.szp.models.ProjectMember;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ProjectMemberRepository;
import com.stg.szp.repos.ProjectRepository;

@Component("projectSecurity")
public class ProjectSecurity {
    
    private final ProjectMemberRepository memberRepo;
    private final ProjectRepository projectRepo;

    ProjectSecurity(ProjectMemberRepository memberRepo, ProjectRepository projectRepo) {
        this.memberRepo = memberRepo;
        this.projectRepo = projectRepo;
    }
    
    public boolean hasAnyRole(SZP_User user, Long projectId, String... roles) {

        if(user == null || projectId == null) return false;


        Optional<Project> projectOpt = projectRepo.findById(projectId);
        if(projectOpt.isPresent() && projectOpt.get().getOwner().getId().equals(user.getId())) {
            if(Arrays.asList(roles).contains("OWNER")) {
                return true;
            }
        }


        Optional<ProjectMember> memberOpt = memberRepo.findByUserIdAndProjectId(user.getId(), projectId);

        if(memberOpt.isEmpty()) return false;

        ProjectMember member = memberOpt.get();
        String userProjectRole = member.getRole().name();

        return Arrays.asList(roles).contains(userProjectRole);
    }
}
