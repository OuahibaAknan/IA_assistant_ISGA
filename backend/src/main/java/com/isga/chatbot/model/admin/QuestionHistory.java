package com.isga.chatbot.model.admin;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "question_history")
public class QuestionHistory {

    @Id
    private String id;

    /*
     * Question utilisateur
     */
    private String question;

    /*
     * Réponse chatbot
     */
    private String answer;

    /*
     * success / not_found / out_of_scope / error
     */
    private String status;

    /*
     * Temps réponse IA
     */
    private long responseTimeMs;

    /*
     * Date question
     */
    private LocalDateTime createdAt;

    /*
     * Sources utilisées
     */
    private List<String> sourceDocuments = new ArrayList<>();

    /*
     * Nombre chunks utilisés
     */
    private int usedChunks;

    /*
     * Score moyen des chunks
     */
    private double averageScore;

    public QuestionHistory() {
    }

    public QuestionHistory(
            String question,
            String answer,
            String status,
            long responseTimeMs,
            List<String> sourceDocuments,
            int usedChunks,
            double averageScore
    ) {
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.responseTimeMs = responseTimeMs;
        this.sourceDocuments = sourceDocuments;
        this.usedChunks = usedChunks;
        this.averageScore = averageScore;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getSourceDocuments() {
        return sourceDocuments;
    }

    public void setSourceDocuments(List<String> sourceDocuments) {
        this.sourceDocuments = sourceDocuments;
    }

    public int getUsedChunks() {
        return usedChunks;
    }

    public void setUsedChunks(int usedChunks) {
        this.usedChunks = usedChunks;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
}