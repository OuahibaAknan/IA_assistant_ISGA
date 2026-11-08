package com.isga.chatbot.service.rag;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/embeddings";

    private final RestTemplate restTemplate = new RestTemplate();

    private final String apiKey;
    private final String model;

    public EmbeddingService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.embedding.model}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public double[] createEmbedding(String text) {

        Map<String, Object> body = Map.of(
                "model", model,
                "input", text
        );

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(
                OPENAI_URL,
                entity,
                Map.class
        );

        List data = (List) response.get("data");
        Map first = (Map) data.get(0);
        List<Double> embeddingList = (List<Double>) first.get("embedding");

        double[] result = new double[embeddingList.size()];

        for (int i = 0; i < embeddingList.size(); i++) {
            result[i] = embeddingList.get(i);
        }

        return result;
    }
}