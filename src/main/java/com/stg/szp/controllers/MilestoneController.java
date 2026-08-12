package com.stg.szp.controllers;

import com.stg.szp.services.MilestoneService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.MilestoneDTO;
import com.stg.szp.models.SZP_User;

@RestController
@RequestMapping("/api/projects/{projectId}/milestones")
public class MilestoneController {
    
    private final MilestoneService milestoneService;

    MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping
    public ResponseEntity<List<MilestoneDTO>> getMilestones(@AuthenticationPrincipal SZP_User user ,@PathVariable Long projectId) {
        if(user == null) return new ResponseEntity<>(HttpStatusCode.valueOf(401));

        List<MilestoneDTO> response = milestoneService.getProjectMilestones(projectId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MilestoneDTO> createMilestone(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId, @RequestBody MilestoneDTO dto) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        MilestoneDTO response = milestoneService.createMilestone(projectId, dto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
