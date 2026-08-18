package com.stg.szp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UploadFileResponse {
    private Long id;
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private Long size;
}
