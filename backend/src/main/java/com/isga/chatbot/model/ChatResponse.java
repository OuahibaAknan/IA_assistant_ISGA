package com.isga.chatbot.model;

// Classe qui représente la réponse du chatbot
public class ChatResponse {

    // Réponse générée
    private String answer;

    // Constructeur avec paramètre
    public ChatResponse(String answer) {
        this.answer = answer;
    }

    // Getter
    public String getAnswer() {
        return answer;
    }

    // Setter
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}