package com.stg.szp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PublicUserProfileDTO {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String bio;
    private String avatarUrl;
}
