package com.stg.szp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    private String refreshToken;
}
