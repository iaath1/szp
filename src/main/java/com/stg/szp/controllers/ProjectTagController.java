package com.stg.szp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.CreateTagDTO;
import com.stg.szp.DTO.TagDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.Tag;
import com.stg.szp.services.TagService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/projects/{projectId}/tags")
@AllArgsConstructor
public class ProjectTagController {

    private final TagService tagService;

    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'OWNER')")
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody CreateTagDTO dto, @PathVariable Long projectId, @AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Tag tag = tagService.createTag(projectId, dto);

        if(tag == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(TagDTO.builder()
            .id(tag.getId())
            .name(tag.getName())
            .colorHex(tag.getColorHex())
            .build(), HttpStatus.OK);
    }

    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'OWNER')")
    @DeleteMapping("/{tagId}")
    public ResponseEntity<?> deleteTag(@AuthenticationPrincipal SZP_User user, @PathVariable Long tagId) {
        boolean res = tagService.deleteTag(tagId);

        if(res) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
