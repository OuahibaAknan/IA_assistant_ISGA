package com.isga.chatbot.service.admin;

import com.isga.chatbot.model.admin.QuestionHistory;
import com.isga.chatbot.model.rag.DocumentChunk;
import com.isga.chatbot.repository.admin.AdminDocumentRepository;
import com.isga.chatbot.repository.admin.QuestionHistoryRepository;
import com.isga.chatbot.service.rag.VectorStoreService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminStatsService {

    private final AdminDocumentRepository documentRepository;
    private final QuestionHistoryRepository historyRepository;
    private final VectorStoreService vectorStoreService;

    public AdminStatsService(
            AdminDocumentRepository documentRepository,
            QuestionHistoryRepository historyRepository,
            VectorStoreService vectorStoreService
    ) {
        this.documentRepository = documentRepository;
        this.historyRepository = historyRepository;
        this.vectorStoreService = vectorStoreService;
    }

    public Map<String, Object> getStats() {
        List<QuestionHistory> history = historyRepository.findAll();
        List<DocumentChunk> chunks = vectorStoreService.getAllChunks();

        long totalDocuments = documentRepository.count();
        long totalQuestions = history.size();

        long indexedDocuments = documentRepository.findAll()
                .stream()
                .filter(doc -> doc.isIndexed())
                .count();

        long unansweredQuestions = history.stream()
                .filter(this::isUnanswered)
                .count();

        double averageResponseTime = history.stream()
                .mapToLong(QuestionHistory::getResponseTimeMs)
                .filter(time -> time > 0)
                .average()
                .orElse(0);

        double averageChunkScore = history.stream()
                .mapToDouble(QuestionHistory::getAverageScore)
                .filter(score -> score > 0)
                .average()
                .orElse(0);

        Map<String, Long> statusStats = history.stream()
                .collect(Collectors.groupingBy(
                        h -> normalizeStatus(h.getStatus(), h.getAnswer()),
                        Collectors.counting()
                ));

        Map<String, Long> documentsUsage = history.stream()
                .filter(h -> h.getSourceDocuments() != null)
                .flatMap(h -> h.getSourceDocuments().stream())
                .filter(doc -> doc != null && !doc.isBlank())
                .collect(Collectors.groupingBy(doc -> doc, Collectors.counting()));

        List<Map<String, Object>> topDocuments = documentsUsage.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("document", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .toList();

        Map<String, Long> topQuestionsRaw = history.stream()
                .map(QuestionHistory::getQuestion)
                .filter(q -> q != null && !q.isBlank())
                .collect(Collectors.groupingBy(q -> q, Collectors.counting()));

        List<Map<String, Object>> popularQuestions = topQuestionsRaw.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("question", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .toList();

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalDocuments", totalDocuments);
        stats.put("indexedDocuments", indexedDocuments);
        stats.put("totalQuestions", totalQuestions);
        stats.put("unansweredQuestions", unansweredQuestions);
        stats.put("averageResponseTime", Math.round(averageResponseTime));
        stats.put("averageChunkScore", Math.round(averageChunkScore * 1000.0) / 1000.0);
        stats.put("totalChunks", chunks.size());
        stats.put("topDocuments", topDocuments);
        stats.put("popularQuestions", popularQuestions);
        stats.put("statusStats", statusStats);

        return stats;
    }

    private boolean isUnanswered(QuestionHistory history) {
        String status = history.getStatus() == null ? "" : history.getStatus().toLowerCase();
        String answer = history.getAnswer() == null ? "" : history.getAnswer().toLowerCase();

        return status.contains("not_found")
                || status.contains("no_indexed_document")
                || status.contains("out_of_scope")
                || status.contains("error")
                || answer.contains("n’est pas disponible")
                || answer.contains("n'est pas disponible")
                || answer.contains("information non disponible")
                || answer.contains("pas disponible dans les documents");
    }

    private String normalizeStatus(String status, String answer) {
        String safeStatus = status == null ? "" : status.toLowerCase();

        if (safeStatus.contains("success")) {
            return "success";
        }

        if (isUnansweredText(safeStatus, answer)) {
            return "not_found_in_documents";
        }

        if (safeStatus.contains("error")) {
            return "error";
        }

        if (safeStatus.isBlank()) {
            return "unknown";
        }

        return safeStatus;
    }

    private boolean isUnansweredText(String status, String answer) {
        String safeAnswer = answer == null ? "" : answer.toLowerCase();

        return status.contains("not_found")
                || status.contains("no_indexed_document")
                || status.contains("out_of_scope")
                || safeAnswer.contains("n’est pas disponible")
                || safeAnswer.contains("n'est pas disponible")
                || safeAnswer.contains("information non disponible")
                || safeAnswer.contains("pas disponible dans les documents");
    }
}