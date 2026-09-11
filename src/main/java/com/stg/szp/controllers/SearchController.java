package com.stg.szp.controllers;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.SearchItemDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.SearchService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchItemDTO>> search(@RequestParam String q, @AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<SearchItemDTO> results = searchService.globalSearch(user, q);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}
