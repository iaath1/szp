package com.stg.szp.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stg.szp.models.Role;
import java.util.List;


public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}
