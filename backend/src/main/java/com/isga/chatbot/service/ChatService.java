package com.isga.chatbot.service;

import com.isga.chatbot.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final WebClient webClient;
    private final String model;

    public ChatService(
            @Value("${openai.api.url}") String openAiApiUrl,
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.model}") String model
    ) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(openAiApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ChatResponse getResponse(String question) {
        try {
            String safeQuestion = question == null ? "" : question.trim();
            if (safeQuestion.isEmpty()) {
                return new ChatResponse("Veuillez saisir une question.");
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("input", List.of(
                    Map.of(
                            "role", "system",
                            "content", List.of(
                                    Map.of(
                                            "type", "input_text",
                                            "text", "Tu es le chatbot officiel de l'institut ISGA. Réponds en français de manière claire, polie et concise. Si l'information n'est pas certaine, dis-le honnêtement."
                                    )
                            )
                    ),
                    Map.of(
                            "role", "user",
                            "content", List.of(
                                    Map.of(
                                            "type", "input_text",
                                            "text", safeQuestion
                                    )
                            )
                    )
            ));

            Map<String, Object> response = webClient.post()
                    .uri("/responses")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String answer = extractOutputText(response);
            if (answer == null || answer.isBlank()) {
                return new ChatResponse("Désolé, je n'ai pas pu générer une réponse.");
            }

            return new ChatResponse(answer.trim());
        } catch (WebClientResponseException e) {
            String details = e.getResponseBodyAsString();
            return new ChatResponse("Erreur OpenAI : " + (details == null || details.isBlank() ? e.getMessage() : details));
        } catch (Exception e) {
            return new ChatResponse("Erreur serveur lors de l'appel à OpenAI : " + e.getMessage());
        }
    }

    private String extractOutputText(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        Object outputText = response.get("output_text");
        if (outputText instanceof String text && !text.isBlank()) {
            return text;
        }

        Object output = response.get("output");
        if (!(output instanceof List<?> outputItems)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (Object itemObj : outputItems) {
            if (!(itemObj instanceof Map<?, ?> itemMap)) {
                continue;
            }

            Object content = itemMap.get("content");
            if (!(content instanceof List<?> contentItems)) {
                continue;
            }

            for (Object contentObj : contentItems) {
                if (!(contentObj instanceof Map<?, ?> contentMap)) {
                    continue;
                }

                Object type = contentMap.get("type");
                Object text = contentMap.get("text");
                if ("output_text".equals(type) && text instanceof String s) {
                    if (!builder.isEmpty()) {
                        builder.append("\n");
                    }
                    builder.append(s);
                }
            }
        }

        return builder.isEmpty() ? null : builder.toString();
    }
}
