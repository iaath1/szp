package com.stg.szp.services;

import org.springframework.stereotype.Service;

import com.stg.szp.DTO.CreateTagDTO;
import com.stg.szp.models.Project;
import com.stg.szp.models.Tag;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.TagRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepo;
    private final ProjectRepository projectRepo;
    
    public Tag createTag(Long projectId, CreateTagDTO dto) {
        Tag tag = new Tag();

        if(projectRepo.findById(projectId).isPresent()) {
            Project project = projectRepo.findById(projectId).get();
            if(project.getTags().stream().filter((tagFilter) -> tagFilter.getName().equals(dto.getName())) != null) {
                tag.setName(dto.getName());
                tag.setColorHex(dto.getColorHex());
                tag.setProject(project);
                tagRepo.save(tag);

                project.getTags().add(tag);
                projectRepo.save(project);
            }
        }

        return tag;
    }

    public boolean deleteTag(Long tagId) {
        if(tagRepo.existsById(tagId)) {
            tagRepo.deleteById(tagId);
            return true;
        }

        return false;
    }
}
