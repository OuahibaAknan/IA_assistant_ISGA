package com.isga.chatbot.model;

// Classe qui représente la requête envoyée par l'utilisateur
public class ChatRequest {

    // La question posée par l'utilisateur
    private String question;

    // Getter pour récupérer la question
    public String getQuestion() {
        return question;
    }

    // Setter pour modifier la question
    public void setQuestion(String question) {
        this.question = question;
    }
}