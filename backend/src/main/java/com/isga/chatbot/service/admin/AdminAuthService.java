package com.isga.chatbot.service.admin;

import com.isga.chatbot.model.admin.Admin;
import com.isga.chatbot.repository.admin.AdminRepository;
import com.isga.chatbot.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        Admin admin = adminRepository.findByEmail(email.trim());

        if (admin == null) {
            return null;
        }

        String storedPassword = admin.getPassword();
        boolean validPassword;

        if (storedPassword != null && storedPassword.startsWith("$2")) {
            validPassword = passwordEncoder.matches(password.trim(), storedPassword);
        } else {
            // Compatibilité avec ton admin actuel stocké en clair : admin123.
            validPassword = storedPassword != null && storedPassword.trim().equals(password.trim());

            // Dès que la connexion réussit, on convertit automatiquement le mot de passe en BCrypt.
            if (validPassword) {
                admin.setPassword(passwordEncoder.encode(password.trim()));
                adminRepository.save(admin);
            }
        }

        if (!validPassword) {
            return null;
        }

        return jwtService.generateToken(admin.getEmail());
    }
}
