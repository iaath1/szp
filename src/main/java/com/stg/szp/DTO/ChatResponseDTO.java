package com.stg.szp.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDTO {
    private Long id;
    private String name;
    private String avatar;
    private String lastMessage;
    private String time;
    private int unread;
}
