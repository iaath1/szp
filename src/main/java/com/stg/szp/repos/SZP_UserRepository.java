package com.stg.szp.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.SZP_User;

@Repository
public interface SZP_UserRepository extends JpaRepository<SZP_User, Long> {
    Optional<SZP_User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query(
        "SELECT u FROM SZP_User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))"
    )
    List<SZP_User> searchUsers(@Param("query") String query);
}
