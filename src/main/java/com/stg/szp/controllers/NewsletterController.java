package com.stg.szp.controllers;

import com.stg.szp.DTO.NewsletterSendRequestDTO;
import com.stg.szp.models.Newsletter;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.NewsletterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/newsletters")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @GetMapping
    public List<Newsletter> getAll() {
        return newsletterService.getAll();
    }

    @PostMapping
    public Newsletter create(@RequestBody Newsletter request, @AuthenticationPrincipal SZP_User user) {
        return newsletterService.createDraft(request.getSubject(), request.getBody(), user);
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<?> send(@PathVariable Long id, @RequestBody NewsletterSendRequestDTO request) {
        newsletterService.sendNewsletter(id, request);
        return ResponseEntity.ok().build();
    }
}
