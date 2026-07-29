package com.stg.szp.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stg.szp.DTO.UserResponseDTO;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.SZP_UserRepository;

@Service
public class UserService {
    
    private final SZP_UserRepository userRepo;
    private final ProjectRepository projectRepository;

    public UserService(SZP_UserRepository userRepo, ProjectRepository projectRepository) {
        this.userRepo = userRepo;
        this.projectRepository = projectRepository;
    }

    public void saveUser(SZP_User user) {
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getSuggestedUsers(SZP_User user, Long projectId) {
        if (user == null || user.getId() == null || projectId == null) {
            return Collections.emptyList();
        }

        Project targetProject = projectRepository.findById(projectId).orElse(null);
        if (targetProject == null) {
            return Collections.emptyList();
        }

        List<Project> userProjects = projectRepository.findAllByOwnerOrMember(user.getId());
        Map<Long, Integer> sharedCount = new HashMap<>();
        Map<Long, SZP_User> sharedUsers = new HashMap<>();
        Map<Long, Boolean> excludedUsers = new HashMap<>();

        if (targetProject.getOwner() != null) {
            excludedUsers.put(targetProject.getOwner().getId(), true);
        }
        if (targetProject.getMembers() != null) {
            for (SZP_User member : targetProject.getMembers()) {
                if (member != null && member.getId() != null) {
                    excludedUsers.put(member.getId(), true);
                }
            }
        }

        for (Project project : userProjects) {
            if (project.getOwner() != null && !project.getOwner().getId().equals(user.getId())
                    && !excludedUsers.containsKey(project.getOwner().getId())) {
                addSharedProject(sharedCount, sharedUsers, project.getOwner());
            }

            if (project.getMembers() != null) {
                for (SZP_User member : project.getMembers()) {
                    if (member != null && member.getId() != null && !member.getId().equals(user.getId())
                            && !excludedUsers.containsKey(member.getId())) {
                        addSharedProject(sharedCount, sharedUsers, member);
                    }
                }
            }
        }

        return sharedCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .map(entry -> mapToUserResponseDTO(sharedUsers.get(entry.getKey())))
            .collect(Collectors.toList());
    }

    private void addSharedProject(Map<Long, Integer> sharedCount, Map<Long, SZP_User> sharedUsers, SZP_User otherUser) {
        if (otherUser == null || otherUser.getId() == null) {
            return;
        }

        Long otherUserId = otherUser.getId();
        sharedUsers.putIfAbsent(otherUserId, otherUser);
        sharedCount.put(otherUserId, sharedCount.getOrDefault(otherUserId, 0) + 1);
    }

    private UserResponseDTO mapToUserResponseDTO(SZP_User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .surname(user.getSurname())
            .avatarUrl(user.getAvatarPath())
            .build();
    }
}
