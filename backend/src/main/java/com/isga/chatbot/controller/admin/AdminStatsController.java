package com.isga.chatbot.controller.admin;

// Service statistiques admin
import com.isga.chatbot.service.admin.AdminStatsService;

// Injection automatique
import org.springframework.beans.factory.annotation.Autowired;

// Imports Spring REST
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Map Java
import java.util.Map;

// Controller REST
@RestController

// URL principale : /api/admin/stats
@RequestMapping("/api/admin/stats")

// Autoriser React
@CrossOrigin(origins = "http://localhost:3000")
public class AdminStatsController {

    // Injection du service stats
    @Autowired
    private AdminStatsService statsService;

    // Endpoint pour récupérer les stats
    @GetMapping
    public Map<String, Object> getStats() {

        // Retourner les stats calculées
        return statsService.getStats();
    }
}