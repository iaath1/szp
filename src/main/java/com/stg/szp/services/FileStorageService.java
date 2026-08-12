package com.stg.szp.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.stg.szp.repos.ProjectFileRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class FileStorageService {
    private final ProjectFileRepository pfRepo;
    private final Path projectPathUrl = Path.of("uploads").toAbsolutePath().normalize();

    public String saveFile(MultipartFile file, String directory) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            // Create target directory (uploads + directory)
            Path targetDir = this.projectPathUrl.resolve(directory).normalize();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetLocation = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative path like "projects/1/uuid_name.jpg"
            return directory + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Saving file failed: " + fileName, e);
        }
    }
    

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.projectPathUrl.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) return resource;
            else throw new RuntimeException("File was not found " + fileName);
        } catch (MalformedURLException e) {
            throw new RuntimeException("File was not found " + fileName, e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.projectPathUrl.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Deleting file failed " + fileName, ex);
        }
    }
}
