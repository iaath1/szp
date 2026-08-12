package com.stg.szp.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
}
