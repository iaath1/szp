package com.stg.szp.security;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.stg.szp.models.ProjectMember;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ProjectMemberRepository;

@Component("projectSecurity")
public class ProjectSecurity {
    
    private final ProjectMemberRepository memberRepo;

    ProjectSecurity(ProjectMemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }
    
    public boolean hasAnyRole(SZP_User user, Long projectId, String... roles) {

        if(user == null || projectId == null) return false;

        Optional<ProjectMember> memberOpt = memberRepo.findByUserIdAndProjectId(user.getId(), projectId);

        if(memberOpt.isEmpty()) return false;

        ProjectMember member = memberOpt.get();
        String userProjectRole = member.getRole().name();

        return Arrays.asList(roles).contains(userProjectRole);
    }
}
