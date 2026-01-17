package com.exammanagement.controller;

import com.exammanagement.dto.DashboardDTO;
import com.exammanagement.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(Authentication authentication) {
        String username = authentication.getName();
        DashboardDTO dashboard = dashboardService.getDashboard(username);
        return ResponseEntity.ok(dashboard);
    }
}
