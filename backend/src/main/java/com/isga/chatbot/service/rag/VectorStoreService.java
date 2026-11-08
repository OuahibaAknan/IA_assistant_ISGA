package com.isga.chatbot.service.rag;

import com.isga.chatbot.model.rag.DocumentChunk;
import com.isga.chatbot.repository.rag.RagChunkRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class VectorStoreService {

    private final RagChunkRepository ragChunkRepository;

    private final List<DocumentChunk> chunks = new ArrayList<>();

    public VectorStoreService(RagChunkRepository ragChunkRepository) {
        this.ragChunkRepository = ragChunkRepository;
    }

    @PostConstruct
    public void loadChunksFromMongoAtStartup() {

        chunks.clear();
        chunks.addAll(ragChunkRepository.findAll());

        System.out.println(
                "✅ RAG chargé depuis MongoDB avec "
                        + chunks.size()
                        + " chunks"
        );
    }

    public void saveChunks(List<DocumentChunk> newChunks) {

        ragChunkRepository.deleteAll();
        ragChunkRepository.saveAll(newChunks);

        chunks.clear();
        chunks.addAll(newChunks);
    }

    public List<DocumentChunk> getAllChunks() {

        if (chunks.isEmpty()) {
            chunks.addAll(ragChunkRepository.findAll());
        }

        return chunks;
    }

    public boolean isEmpty() {
        return getAllChunks().isEmpty();
    }

    /*
     * HYBRID SEARCH :
     * embedding similarity + keyword matching
     */
    public List<DocumentChunk> searchSimilarChunks(
            double[] questionEmbedding,
            String question,
            int limit
    ) {

        String normalizedQuestion = normalize(question);

        return getAllChunks().stream()

                .filter(chunk ->
                        chunk.getEmbedding() != null &&
                                chunk.getContent() != null
                )

                .peek(chunk -> {

                    /*
                     * Similarité vectorielle
                     */
                    double embeddingScore =
                            cosineSimilarity(
                                    questionEmbedding,
                                    chunk.getEmbedding()
                            );

                    /*
                     * Score mots clés
                     */
                    double keywordScore =
                            calculateKeywordScore(
                                    normalizedQuestion,
                                    normalize(chunk.getContent())
                            );

                    /*
                     * Score hybride
                     * 75% embedding
                     * 25% keywords
                     */
                    double hybridScore =
                            (embeddingScore * 0.75)
                                    + (keywordScore * 0.25);

                    chunk.setScore(embeddingScore);
                    chunk.setKeywordScore(keywordScore);
                    chunk.setHybridScore(hybridScore);
                })

                .sorted(
                        Comparator.comparingDouble(
                                DocumentChunk::getHybridScore
                        ).reversed()
                )

                .limit(limit)

                .toList();
    }

    /*
     * Score mots clés simple
     */
    private double calculateKeywordScore(
            String question,
            String content
    ) {

        if (question == null || content == null) {
            return 0.0;
        }

        String[] words = question.split("\\s+");

        if (words.length == 0) {
            return 0.0;
        }

        int matches = 0;

        for (String word : words) {

            if (word.length() < 3) {
                continue;
            }

            if (content.contains(word)) {
                matches++;
            }
        }

        return (double) matches / words.length;
    }

    /*
     * Nettoyage texte
     */
    private String normalize(String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9àâçéèêëîïôûùüÿñæœ\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /*
     * Cosine similarity
     */
    private double cosineSimilarity(
            double[] vectorA,
            double[] vectorB
    ) {

        if (vectorA == null ||
                vectorB == null ||
                vectorA.length == 0 ||
                vectorB.length == 0) {
            return 0.0;
        }

        if (vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {

            dotProduct += vectorA[i] * vectorB[i];

            normA += vectorA[i] * vectorA[i];

            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (
                Math.sqrt(normA) * Math.sqrt(normB)
        );
    }
}