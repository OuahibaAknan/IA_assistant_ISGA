package com.isga.chatbot.model.rag;

import java.util.List;

// Cette classe représente la requête envoyée par React ou Postman vers /api/rag/ask
public class RagRequest {

    // Question actuelle de l'utilisateur
    private String question;

    // Historique court de la conversation actuelle
    private List<RagConversationMessage> history;

    // Constructeur vide obligatoire pour Spring
    public RagRequest() {
    }

    // Constructeur avec paramètres
    public RagRequest(String question, List<RagConversationMessage> history) {
        this.question = question;
        this.history = history;
    }

    // Getter question
    public String getQuestion() {
        return question;
    }

    // Setter question
    public void setQuestion(String question) {
        this.question = question;
    }

    // Getter history
    public List<RagConversationMessage> getHistory() {
        return history;
    }

    // Setter history
    public void setHistory(List<RagConversationMessage> history) {
        this.history = history;
    }
}