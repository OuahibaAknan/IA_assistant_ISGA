package com.isga.chatbot.model.rag;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rag_chunks")
public class DocumentChunk {

    @Id
    private String mongoId;

    private int id;

    private String documentId;

    private String sourceName;

    private String content;

    private double[] embedding;

    private double score;

    /*
     * Nouveau :
     * score hybride = embedding + mots clés
     */
    private double keywordScore;

    private double hybridScore;

    /*
     * Nombre de fois utilisé dans réponses
     */
    private long usageCount;

    public DocumentChunk() {
    }

    public DocumentChunk(int id, String content, double[] embedding) {
        this.id = id;
        this.content = content;
        this.embedding = embedding;
    }

    public String getMongoId() {
        return mongoId;
    }

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(double[] embedding) {
        this.embedding = embedding;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getKeywordScore() {
        return keywordScore;
    }

    public void setKeywordScore(double keywordScore) {
        this.keywordScore = keywordScore;
    }

    public double getHybridScore() {
        return hybridScore;
    }

    public void setHybridScore(double hybridScore) {
        this.hybridScore = hybridScore;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(long usageCount) {
        this.usageCount = usageCount;
    }
}