package com.isga.chatbot.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    // Exemple très simple : vérification de credentials hardcodée
    public boolean checkCredentials(String email, String password) {
        // Ici tu peux remplacer par une vraie vérification MongoDB si tu veux
        return "user@example.com".equals(email) && "password".equals(password);
    }
}