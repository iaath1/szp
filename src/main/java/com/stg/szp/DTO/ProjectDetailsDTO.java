package com.stg.szp.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stg.szp.models.SZP_User;
import com.stg.szp.models.Task;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDetailsDTO {
    private Long id;

    private String title;

    private String description;

    private SZP_User owner;

    private LocalDateTime createdAt = LocalDateTime.now();

    private Set<SZP_User> members = new HashSet<>();

    private List<Task> tasks = new ArrayList<>();
}
