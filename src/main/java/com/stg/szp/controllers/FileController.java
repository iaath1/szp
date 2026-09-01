package com.stg.szp.controllers;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpHeaders;

import com.stg.szp.DTO.UploadFileResponse;
import com.stg.szp.models.ProjectFile;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ProjectFileRepository;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.services.ActivityLogService;
import com.stg.szp.services.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FileController {
    
    private final FileStorageService fileService;
    private final ProjectFileRepository pfRepo;
    private final ProjectRepository projectRepo;
    private final ActivityLogService activityService;

    @PostMapping("/upload/")
    public ResponseEntity<UploadFileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileService.saveFile(file, "common");
        

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/files/download")
            .queryParam("fileName", fileName)
            .toUriString();

        UploadFileResponse response = new UploadFileResponse(null, fileName, fileDownloadUri, file.getContentType(), file.getSize());
    
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'OWNER')")
    @PostMapping("/upload/project/{projectId}")
    public ResponseEntity<UploadFileResponse> uploadProjectFile(
            @PathVariable("projectId") Long projectId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal SZP_User user
        ) {
        
        String targetDirectory = "projects/" + projectId;
        String fileName = fileService.saveFile(file, targetDirectory);

        ProjectFile pfToSave = new ProjectFile();
        pfToSave.setOriginalName(file.getOriginalFilename());
        pfToSave.setContentType(file.getContentType());
        pfToSave.setSize(file.getSize());
        pfToSave.setStoredName(fileName);

        // Needs to be added verification is this project exists
        pfToSave.setProject(projectRepo.findById(projectId).get());
        pfToSave.setUploader(user);
        pfToSave.setUploadetAt(LocalDateTime.now());
        
        activityService.logActivity("FILE_UPLOADED", "file '" + fileName + "'was uploaded to project " + pfToSave.getProject().getTitle(), pfToSave.getProject(), user);

        pfRepo.save(pfToSave);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/files/download")
            .queryParam("fileName", fileName)
            .toUriString();

        UploadFileResponse response = new UploadFileResponse(pfToSave.getId(), fileName, fileDownloadUri, file.getContentType(), file.getSize());
    
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileName") String fileName, HttpServletRequest req) {
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

    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'OWNER')")
    @DeleteMapping("/project/{projectId}/file/{fileId}")
    public ResponseEntity<?> deleteProjectFile(@PathVariable("projectId") Long projectId, @PathVariable("fileId") Long fileId) {
        ProjectFile file = pfRepo.findById(fileId).orElse(null);
        if (file == null || !file.getProject().getId().equals(projectId)) {
            return ResponseEntity.notFound().build();
        }
        
        pfRepo.delete(file);
        return ResponseEntity.ok().build();
    }
}
