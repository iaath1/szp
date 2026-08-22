package com.stg.szp.controllers;

import com.stg.szp.DTO.ReportResponseDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<ReportResponseDTO> getReport(
            @RequestParam(defaultValue = "This Week") String period,
            @AuthenticationPrincipal SZP_User user) {

        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        ReportResponseDTO report = reportService.generateReport(user, period);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}

