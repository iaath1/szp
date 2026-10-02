package com.stg.szp.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor 
@NoArgsConstructor 
@Data
@Builder 
public class NewsletterSendRequestDTO {
    private boolean sendToAll;
    private Long projectId;
    private List<Long> userIds;
}
