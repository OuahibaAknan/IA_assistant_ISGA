package com.isga.chatbot.model.rag;

public class RagSource {

    /*
     * Id document MongoDB
     */
    private String documentId;

    /*
     * Nom du document source
     */
    private String sourceName;

    /*
     * Numéro chunk
     */
    private int chunkId;

    /*
     * Score embedding
     */
    private double score;

    /*
     * Score mots clés
     */
    private double keywordScore;

    /*
     * Score hybride final
     */
    private double hybridScore;

    /*
     * Extrait chunk
     */
    private String preview;

    /*
     * Nombre d’utilisations
     */
    private long usageCount;

    public RagSource() {
    }

    public RagSource(
            String documentId,
            String sourceName,
            int chunkId,
            double score,
            double keywordScore,
            double hybridScore,
            String preview,
            long usageCount
    ) {
        this.documentId = documentId;
        this.sourceName = sourceName;
        this.chunkId = chunkId;
        this.score = score;
        this.keywordScore = keywordScore;
        this.hybridScore = hybridScore;
        this.preview = preview;
        this.usageCount = usageCount;
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

    public int getChunkId() {
        return chunkId;
    }

    public void setChunkId(int chunkId) {
        this.chunkId = chunkId;
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

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(long usageCount) {
        this.usageCount = usageCount;
    }
}