package com.isga.chatbot.model.rag;

// Cette classe représente un ancien message de la conversation
public class RagConversationMessage {

    // role = "user" ou "bot"
    private String role;

    // contenu du message
    private String content;

    // Constructeur vide obligatoire
    public RagConversationMessage() {
    }

    // Constructeur avec paramètres
    public RagConversationMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // Getter role
    public String getRole() {
        return role;
    }

    // Setter role
    public void setRole(String role) {
        this.role = role;
    }

    // Getter content
    public String getContent() {
        return content;
    }

    // Setter content
    public void setContent(String content) {
        this.content = content;
    }
}