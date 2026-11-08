package com.isga.chatbot.model.admin;

// Permet de définir l'identifiant MongoDB
import org.springframework.data.annotation.Id;

// Permet de dire que cette classe sera une collection MongoDB
import org.springframework.data.mongodb.core.mapping.Document;

// Cette annotation crée une collection MongoDB appelée "admins"
@Document(collection = "admins")
public class Admin {

    // Identifiant unique généré par MongoDB
    @Id
    private String id;

    // Email de l'administrateur
    private String email;

    // Mot de passe de l'administrateur
    private String password;

    // Constructeur vide obligatoire pour MongoDB
    public Admin() {
    }

    // Constructeur avec email et password
    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter de id
    public String getId() {
        return id;
    }

    // Setter de id
    public void setId(String id) {
        this.id = id;
    }

    // Getter de email
    public String getEmail() {
        return email;
    }

    // Setter de email
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter de password
    public String getPassword() {
        return password;
    }

    // Setter de password
    public void setPassword(String password) {
        this.password = password;
    }
}