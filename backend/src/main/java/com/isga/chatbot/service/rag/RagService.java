package com.isga.chatbot.service.rag;

import com.isga.chatbot.model.rag.DocumentChunk;
import com.isga.chatbot.model.rag.RagConversationMessage;
import com.isga.chatbot.model.rag.RagResponse;
import com.isga.chatbot.model.rag.RagSource;
import com.isga.chatbot.service.admin.AdminHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final AdminHistoryService adminHistoryService;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final double MINIMUM_SCORE = 0.35;
    private static final int TOP_K = 8;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.chat.model}")
    private String chatModel;

    public RagService(
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService,
            AdminHistoryService adminHistoryService
    ) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.adminHistoryService = adminHistoryService;
    }

    public RagResponse ask(String question) {
        return ask(question, List.of());
    }

    public RagResponse ask(String question, List<RagConversationMessage> history) {

        if (question == null || question.trim().isEmpty()) {
            return new RagResponse(
                    "Veuillez écrire une question claire afin que je puisse vous aider.",
                    "error",
                    List.of()
            );
        }

        String cleanQuestion = question.trim();
        String conversationContext = buildConversationContext(history);

        if (vectorStoreService.isEmpty()) {
            String prompt = buildNoDocumentPrompt(cleanQuestion, conversationContext);
            String answer = callOpenAI(prompt);
            String finalStatus = "no_indexed_document";

            adminHistoryService.saveQuestion(
                    cleanQuestion,
                    answer,
                    finalStatus,
                    0,
                    List.of(),
                    0,
                    0
            );

            return new RagResponse(answer, finalStatus, List.of());
        }

        String searchQuestion = buildSearchQuestion(cleanQuestion, conversationContext);
        double[] questionEmbedding = embeddingService.createEmbedding(searchQuestion);

        List<DocumentChunk> bestChunks = vectorStoreService.searchSimilarChunks(
                questionEmbedding,
                searchQuestion,
                TOP_K
        );

        List<DocumentChunk> acceptedChunks = bestChunks.stream()
                .filter(chunk -> chunk.getHybridScore() >= MINIMUM_SCORE)
                .toList();

        if (acceptedChunks.isEmpty()) {
            String prompt = buildNoRelevantContextPrompt(cleanQuestion, conversationContext);
            String answer = callOpenAI(prompt);
            String finalStatus = "not_found_in_documents";

            adminHistoryService.saveQuestion(
                    cleanQuestion,
                    answer,
                    finalStatus,
                    0,
                    List.of(),
                    0,
                    0
            );

            return new RagResponse(answer, finalStatus, List.of());
        }

        String context = buildContext(acceptedChunks);
        String prompt = buildAdvancedRagPrompt(cleanQuestion, conversationContext, context);

        long startTime = System.currentTimeMillis();
        String answer = callOpenAI(prompt);
        long endTime = System.currentTimeMillis();

        String finalStatus = detectAnswerStatus(answer);

        List<RagSource> sources =
                finalStatus.equals("success")
                        ? buildSources(acceptedChunks)
                        : List.of();

        List<String> sourceDocuments =
                finalStatus.equals("success")
                        ? acceptedChunks.stream()
                        .map(DocumentChunk::getSourceName)
                        .distinct()
                        .toList()
                        : List.of();

        double averageScore =
                finalStatus.equals("success")
                        ? acceptedChunks.stream()
                        .mapToDouble(DocumentChunk::getHybridScore)
                        .average()
                        .orElse(0)
                        : 0;

        adminHistoryService.saveQuestion(
                cleanQuestion,
                answer,
                finalStatus,
                endTime - startTime,
                sourceDocuments,
                finalStatus.equals("success") ? acceptedChunks.size() : 0,
                Math.round(averageScore * 1000.0) / 1000.0
        );

        return new RagResponse(answer, finalStatus, sources);
    }

    private String detectAnswerStatus(String answer) {

        if (answer == null || answer.isBlank()) {
            return "error";
        }

        String clean = answer.toLowerCase();

        if (
                clean.contains("information n’est pas disponible")
                        || clean.contains("information n'est pas disponible")
                        || clean.contains("cette information n’est pas disponible")
                        || clean.contains("cette information n'est pas disponible")
                        || clean.contains("pas disponible dans les documents")
                        || clean.contains("absente des documents")
                        || clean.contains("aucun extrait fiable")
                        || clean.contains("aucun document")
                        || clean.contains("je suis spécialisé")
                        || clean.contains("hors sujet")
        ) {
            return "not_found_in_documents";
        }

        return "success";
    }

    private String buildConversationContext(List<RagConversationMessage> history) {

        if (history == null || history.isEmpty()) {
            return "Aucun historique de conversation fourni.";
        }

        StringBuilder builder = new StringBuilder();
        int start = Math.max(0, history.size() - 6);

        for (int i = start; i < history.size(); i++) {
            RagConversationMessage message = history.get(i);

            if (message == null || message.getContent() == null) {
                continue;
            }

            String role = message.getRole() == null ? "user" : message.getRole();
            String content = message.getContent().trim();

            if (content.isEmpty()) {
                continue;
            }

            if (content.length() > 700) {
                content = content.substring(0, 700) + "...";
            }

            builder.append(role)
                    .append(" : ")
                    .append(content)
                    .append("\n");
        }

        return builder.toString();
    }

    private String buildSearchQuestion(String question, String conversationContext) {
        return """
Historique récent :
%s

Question actuelle :
%s
""".formatted(conversationContext, question);
    }

    private String buildContext(List<DocumentChunk> chunks) {

        StringBuilder context = new StringBuilder();

        for (DocumentChunk chunk : chunks) {
            context.append("SOURCE: ")
                    .append(chunk.getSourceName())
                    .append(" | CHUNK: ")
                    .append(chunk.getId())
                    .append(" | EMBEDDING SCORE: ")
                    .append(String.format("%.3f", chunk.getScore()))
                    .append(" | KEYWORD SCORE: ")
                    .append(String.format("%.3f", chunk.getKeywordScore()))
                    .append(" | HYBRID SCORE: ")
                    .append(String.format("%.3f", chunk.getHybridScore()))
                    .append("\n")
                    .append(chunk.getContent())
                    .append("\n\n");
        }

        return context.toString();
    }

    private List<RagSource> buildSources(List<DocumentChunk> chunks) {

        List<RagSource> sources = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            String preview = chunk.getContent();

            if (preview.length() > 180) {
                preview = preview.substring(0, 180) + "...";
            }

            chunk.setUsageCount(chunk.getUsageCount() + 1);

            sources.add(
                    new RagSource(
                            chunk.getDocumentId(),
                            chunk.getSourceName(),
                            chunk.getId(),
                            Math.round(chunk.getScore() * 1000.0) / 1000.0,
                            Math.round(chunk.getKeywordScore() * 1000.0) / 1000.0,
                            Math.round(chunk.getHybridScore() * 1000.0) / 1000.0,
                            preview,
                            chunk.getUsageCount()
                    )
            );
        }

        return sources;
    }

    private String buildAdvancedRagPrompt(
            String question,
            String conversationContext,
            String context
    ) {
        return """
Tu es l’assistant officiel de l’ISGA.

OBJECTIF :
Répondre de manière professionnelle, naturelle et utile aux visiteurs ou étudiants de l’ISGA.

HISTORIQUE :
%s

COMPORTEMENT :
1. Réponds naturellement aux salutations, remerciements et au revoir.
2. Réponds uniquement avec les informations du CONTEXTE.
3. Si l’information est absente, dis exactement :
"Cette information n’est pas disponible dans les documents fournis."
4. Si la question est hors sujet, explique que tu réponds uniquement aux questions liées à l’ISGA.
5. Réponds dans la même langue que l’utilisateur.
6. Combine intelligemment les extraits.
7. Utilise une réponse claire et structurée.
8. N’invente jamais d’informations.

CONTEXTE DOCUMENTAIRE :
%s

QUESTION :
%s

RÉPONSE :
""".formatted(conversationContext, context, question);
    }

    private String buildNoRelevantContextPrompt(
            String question,
            String conversationContext
    ) {
        return """
Tu es l’assistant officiel de l’ISGA.

Aucun extrait fiable n’a été trouvé.

HISTORIQUE :
%s

COMPORTEMENT :
1. Réponds naturellement aux salutations.
2. Réponds naturellement aux remerciements.
3. Réponds poliment aux au revoir.
4. Si la question est ISGA mais absente des documents, réponds exactement :
"Cette information n’est pas disponible dans les documents fournis."
5. Si hors sujet :
"Je suis spécialisé dans les informations liées à l’ISGA."
6. Réponds dans la langue de l’utilisateur.
7. N’invente rien.

QUESTION :
%s

RÉPONSE :
""".formatted(conversationContext, question);
    }

    private String buildNoDocumentPrompt(
            String question,
            String conversationContext
    ) {
        return """
Tu es l’assistant officiel de l’ISGA.

Aucun document n’est indexé.

HISTORIQUE :
%s

COMPORTEMENT :
1. Réponds naturellement aux salutations.
2. Réponds naturellement aux remerciements.
3. Réponds naturellement aux au revoir.
4. Explique que les documents doivent être ajoutés depuis le dashboard admin.
5. Réponds dans la langue de l’utilisateur.
6. N’invente aucune information.

QUESTION :
%s

RÉPONSE :
""".formatted(conversationContext, question);
    }

    private String callOpenAI(String prompt) {

        Map<String, Object> body = Map.of(
                "model",
                chatModel,
                "messages",
                List.of(
                        Map.of(
                                "role",
                                "system",
                                "content",
                                """
Tu es un assistant officiel de l’ISGA.
Tu es professionnel, naturel, fiable, multilingue et intelligent.
Tu comprends le contexte conversationnel récent.
Tu ne dois jamais inventer des informations absentes des documents.
"""
                        ),
                        Map.of(
                                "role",
                                "user",
                                "content",
                                prompt
                        )
                ),
                "temperature",
                0.2,
                "max_tokens",
                750
        );

        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(
                OPENAI_CHAT_URL,
                entity,
                Map.class
        );

        if (response == null || !response.containsKey("choices")) {
            return "Erreur : aucune réponse reçue du modèle.";
        }

        List choices = (List) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            return "Erreur : réponse vide du modèle.";
        }

        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        if (message == null || message.get("content") == null) {
            return "Erreur : contenu de réponse indisponible.";
        }

        return message.get("content").toString().trim();
    }
}