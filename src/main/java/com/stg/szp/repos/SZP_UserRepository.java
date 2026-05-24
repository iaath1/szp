package com.stg.szp.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.SZP_User;

@Repository
public interface SZP_UserRepository extends JpaRepository<SZP_User, Long> {
    Optional<SZP_User> findByEmail(String email);
    boolean existsByEmail(String email);
}
