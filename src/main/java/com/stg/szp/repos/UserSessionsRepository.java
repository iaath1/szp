package com.stg.szp.repos;


import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stg.szp.models.UserSessions;

@Repository
public interface UserSessionsRepository extends JpaRepository<UserSessions, Long>{
    List<UserSessions> findAllByUserIdAndExpiresAtAfter(Long userId, Timestamp expiresAt);

    @Transactional
    void deleteByIdAndUserId(Long sessionId, Long userId);

    Optional<UserSessions> findByUserIdAndDeviceInfoAndIpAddress(Long userId, String deviceInfo, String ipAddress);
}
