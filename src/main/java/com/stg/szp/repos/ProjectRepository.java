package com.stg.szp.repos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Project;
import com.stg.szp.models.ProjectStatus;
import com.stg.szp.models.SZP_User;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwner(SZP_User owner);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)")
    List<Project> findAllByOwnerOrMember(@Param("userId") Long userId);

    Long countByOwnerIdAndStatus(Long userId, ProjectStatus status);
    Long countByMembers_IdAndStatus(Long userId, ProjectStatus status);
    Long countByOwnerId(Long userId);
    boolean existsByOwnerIdAndProjectKey(Long ownerId, String projectKey);
    Optional<Project> findByOwnerIdAndProjectKey(Long ownerId, String projectKey);

    @Query("SELECT p FROM Project p WHERE (p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)) AND p.status = :status")
    List<Project> findAllByOwnerOrMemberAndStatus(@Param("userId") Long userId, @Param("status") ProjectStatus status);

    // Переделать так что бы считало и те проекты где пользователь как администратор
    Long countByMembers_Id(Long userId);

    @Query("SELECT COUNT(p) FROM Project p WHERE (p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)) AND createdAt >= :startDate")
    Long countNewProjectsSince(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
}
