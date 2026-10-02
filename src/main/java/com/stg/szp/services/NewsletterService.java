package com.stg.szp.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.stg.szp.DTO.NewsletterSendRequestDTO;
import com.stg.szp.models.Newsletter;
import com.stg.szp.models.NewsletterStatus;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.NewsletterRepository;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.SZP_UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsletterService {
    private final NewsletterRepository letterRepo;
    private final SZP_UserRepository userRepo;
    private final EmailService emailService;
    private final ProjectRepository projectRepository;

    public Newsletter createDraft(String subject, String body, SZP_User author) {
        Newsletter letter = new Newsletter();
        letter.setSubject(subject);
        letter.setBody(body);
        letter.setAuthor(author);
        letter.setCreatedAt(LocalDateTime.now());
        letter.setStatus(NewsletterStatus.DRAFT);

        return letterRepo.save(letter);
    }

    public void sendNewsletter(Long id, NewsletterSendRequestDTO req) {
        Newsletter letter = letterRepo.findById(id).orElseThrow();
        Set<String> targetEmails = new HashSet<>();

        if(req.isSendToAll()) {
            userRepo.findAll().forEach(user -> {
                if(user.getEmail() != null) targetEmails.add(user.getEmail());
            });
        } else if(req.getProjectId() != null) {
            Project project = projectRepository.findById(req.getProjectId()).orElseThrow();
            project.getMembers().forEach(user -> {
                if(user.getEmail() != null) targetEmails.add(user.getEmail());
            });

            if(project.getOwner() != null && project.getOwner().getEmail() != null) {
                targetEmails.add(project.getOwner().getEmail());
            }
        } else if(req.getUserIds() != null && !req.getUserIds().isEmpty()) {
            userRepo.findAllById(req.getUserIds()).forEach(user -> {
                if(user.getEmail() != null) targetEmails.add(user.getEmail());
            });
        }

        for(String email : targetEmails) {
            emailService.sendEmail(email, letter.getSubject(), letter.getBody());
        }

        letter.setSentAt(LocalDateTime.now());
        letter.setStatus(NewsletterStatus.SENT);

        letterRepo.save(letter);
    }

    public List<Newsletter> getAll() {
        return letterRepo.findAll();
    }
}
