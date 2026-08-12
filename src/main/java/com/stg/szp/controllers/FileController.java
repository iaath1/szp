package com.stg.szp.controllers;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpHeaders;

import com.stg.szp.DTO.UploadFileResponse;
import com.stg.szp.services.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FileController {
    
    private final FileStorageService fileService;

    @PostMapping("/upload")
    public ResponseEntity<UploadFileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileService.saveFile(file, "common");

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/files/download/")
            .path(fileName)
            .toUriString();

        UploadFileResponse response = new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'OWNER')")
    @PostMapping("/upload/project/{projectId}")
    public ResponseEntity<UploadFileResponse> uploadProjectFile(
            @PathVariable("projectId") Long projectId,
            @RequestParam("file") MultipartFile file
        ) {
        
        String targetDirectory = "projects/" + projectId;
        String fileName = fileService.saveFile(file, targetDirectory);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/files/download/")
            .path(fileName)
            .toUriString();

        UploadFileResponse response = new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest req) {
        Resource resource = fileService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = req.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch(IOException e) {
            // Ignore if type is undefined
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\\*" + resource.getFilename() + "\"")
            .body(resource);
    }
}
