package com.stg.szp.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponseDTO {
    private Long id;
    private String sender;
    private String text;
    private String time;
    private boolean isMine;
}
