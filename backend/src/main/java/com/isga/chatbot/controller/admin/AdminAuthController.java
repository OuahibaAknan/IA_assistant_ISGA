package com.isga.chatbot.controller.admin;

import com.isga.chatbot.service.admin.AdminAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        String token = adminAuthService.login(email, password);

        if (token != null) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Connexion réussie",
                    "token", token
            ));
        }

        return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Identifiants incorrects"
        ));
    }
}
