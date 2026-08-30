package tn.esprit.test.stroke_backend.controller;

import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.esprit.test.stroke_backend.services.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        return dashboardService.getStatistics();
    }

    @GetMapping("/recent-analyses")
    public ResponseEntity<?> getRecentAnalyses() {
        return dashboardService.getRecentAnalyses();
    }
}