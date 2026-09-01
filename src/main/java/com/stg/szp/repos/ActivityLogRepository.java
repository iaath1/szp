package com.stg.szp.repos;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>{
    @Query("SELECT a FROM ActivityLog a WHERE a.project.owner.id = :userId OR :userId IN (SELECT m.id FROM a.project.members m) ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentActivitiesForUser(@Param("userId") Long userId, Pageable pageable);
}
