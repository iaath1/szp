package com.stg.szp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SearchItemDTO {
    private Long id;
    private String title;
    private String subtitle;
    private String type;
    private String url;
}
