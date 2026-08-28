package com.stg.szp.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.stg.szp.DTO.ChangePasswordDTO;
import com.stg.szp.DTO.NotificationsPreferencesDTO;
import com.stg.szp.DTO.PublicUserProfileDTO;
import com.stg.szp.DTO.UserProfileUpdateDTO;
import com.stg.szp.DTO.UserResponseDTO;
import com.stg.szp.models.NotificationPreferences;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.SZP_UserRepository;

@Service
public class UserService {
    
    private final SZP_UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final Path userPathUrl = Path.of("uploads").toAbsolutePath().normalize();

    public UserService(SZP_UserRepository userRepo, ProjectRepository projectRepository, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void saveUser(SZP_User user) {
        userRepo.save(user);
    }

    public void changePassword(SZP_User user, ChangePasswordDTO dto) {
        if(!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid current password");
        }

        if(passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cant be the same as old password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
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

    public UserResponseDTO updateUser(SZP_User userToUpdate, UserProfileUpdateDTO dto) {
        userToUpdate.setBio(dto.getBio());
        userToUpdate.setName(dto.getName());
        userToUpdate.setSurname(dto.getSurname());
        userToUpdate.setAccentColor(dto.getAccentColor());
        userToUpdate.setCompactMode(dto.isCompactMode());
        userToUpdate.setTheme(dto.getTheme());

        userRepo.save(userToUpdate);
        
        return mapToUserResponseDTO(userToUpdate);
    }

    public UserResponseDTO uploadNewAvatar(SZP_User user, MultipartFile avatar) {
        String targetDirectory = "users/" + user.getId();
        String fileName = saveAvatar(avatar, targetDirectory);

        user.setAvatarPath(fileName);
        return mapToUserResponseDTO(userRepo.save(user));
    }

    public String saveAvatar(MultipartFile avatar, String directory) {
        String originalFileName = StringUtils.cleanPath(avatar.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            // Create target directory (uploads + directory)
            Path targetDir = this.userPathUrl.resolve(directory).normalize();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetLocation = targetDir.resolve(fileName);
            Files.copy(avatar.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative path like "projects/1/uuid_name.jpg"
            return directory + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Saving file failed: " + fileName, e);
        }
    }

    private void addSharedProject(Map<Long, Integer> sharedCount, Map<Long, SZP_User> sharedUsers, SZP_User otherUser) {
        if (otherUser == null || otherUser.getId() == null) {
            return;
        }

        Long otherUserId = otherUser.getId();
        sharedUsers.putIfAbsent(otherUserId, otherUser);
        sharedCount.put(otherUserId, sharedCount.getOrDefault(otherUserId, 0) + 1);
    }

    public UserResponseDTO mapToUserResponseDTO(SZP_User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .surname(user.getSurname())
            .avatarUrl(user.getAvatarPath())
            .bio(user.getBio())
            .notifications(mapToNotificationsPreferencesDTO(user.getNotificationPreferences()))
            .mfaEnabled(user.isMfaEnabled())
            .theme(user.getTheme())
            .accentColor(user.getAccentColor())
            .compactMode(user.isCompactMode())
            .build();
    }

    public NotificationsPreferencesDTO mapToNotificationsPreferencesDTO(NotificationPreferences notifications) {
        if(notifications == null) {
            notifications = new NotificationPreferences();
            notifications.setEmailNotifications(true);
            notifications.setMentions(true);
            notifications.setProjectInvites(true);
            notifications.setPushNotifications(true);
            notifications.setTaskUpdated(true);
        }

        return NotificationsPreferencesDTO.builder()
            .emailNotifications(notifications.isEmailNotifications())
            .mentions(notifications.isMentions())
            .projectInvites(notifications.isProjectInvites())
            .pushNotifications(notifications.isPushNotifications())
            .taskUpdated(notifications.isTaskUpdated())
            .build();
    }

    public UserResponseDTO updateNotifications(SZP_User user, NotificationsPreferencesDTO dto) {
        NotificationPreferences notifications = new NotificationPreferences();
        notifications.setEmailNotifications(dto.isEmailNotifications());
        notifications.setMentions(dto.isMentions());
        notifications.setProjectInvites(dto.isProjectInvites());
        notifications.setPushNotifications(dto.isPushNotifications());
        notifications.setTaskUpdated(dto.isTaskUpdated());

        user.setNotificationPreferences(notifications);
        userRepo.save(user);

        return mapToUserResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getMyTeam(SZP_User user) {
        if (user == null || user.getId() == null) {
            return Collections.emptyList();
        }

        List<Project> userProjects = projectRepository.findAllByOwnerOrMember(user.getId());
        Map<Long, SZP_User> teamMembers = new HashMap<>();

        for (Project project : userProjects) {
            if (project.getOwner() != null && !project.getOwner().getId().equals(user.getId())) {
                teamMembers.putIfAbsent(project.getOwner().getId(), project.getOwner());
            }

            if (project.getMembers() != null) {
                for (SZP_User member : project.getMembers()) {
                    if (member != null && member.getId() != null && !member.getId().equals(user.getId())) {
                        teamMembers.putIfAbsent(member.getId(), member);
                    }
                }
            }
        }

        return teamMembers.values().stream()
            .map(this::mapToUserResponseDTO)
            .collect(Collectors.toList());
    }

    public PublicUserProfileDTO getUserPublicProfile(Long userId) {
        SZP_User user = userRepo.findById(userId).orElse( null);

        if(user == null) return null;

        return PublicUserProfileDTO.builder()
            .id(userId)
            .name(user.getName())
            .surname(user.getSurname())
            .email(user.getEmail())
            .bio(user.getBio())
            .avatarUrl(user.getAvatarPath())
            .build();
    }

}
