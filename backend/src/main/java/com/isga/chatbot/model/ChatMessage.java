package com.isga.chatbot.model;

public class ChatMessage {

    private String sender;
    private String message;

    // Constructeur par défaut
    public ChatMessage() {}

    // Constructeur avec arguments
    public ChatMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    // Getters et setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}