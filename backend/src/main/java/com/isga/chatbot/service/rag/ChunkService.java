package com.isga.chatbot.service.rag;

import com.isga.chatbot.model.rag.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    private static final int MAX_CHARS = 1200;
    private static final int OVERLAP_CHARS = 200;

    public List<DocumentChunk> splitTextIntoChunks(String text) {
        return splitTextIntoChunks(text, null, null);
    }

    public List<DocumentChunk> splitTextIntoChunks(String text, String documentId, String sourceName) {
        List<DocumentChunk> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        String normalizedText = text
                .replace("\r", "\n")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();

        String[] paragraphs = normalizedText.split("\n\s*\n");
        StringBuilder currentChunk = new StringBuilder();
        int chunkId = 0;

        for (String paragraph : paragraphs) {
            String cleanParagraph = paragraph.trim();

            if (cleanParagraph.length() < 30) {
                continue;
            }

            if (currentChunk.length() + cleanParagraph.length() > MAX_CHARS) {
                chunkId = addChunk(chunks, currentChunk.toString(), chunkId, documentId, sourceName);
                currentChunk = new StringBuilder(getOverlap(currentChunk.toString()));
            }

            if (!currentChunk.isEmpty()) {
                currentChunk.append("\n");
            }

            currentChunk.append(cleanParagraph);
        }

        addChunk(chunks, currentChunk.toString(), chunkId, documentId, sourceName);
        return chunks;
    }

    private int addChunk(List<DocumentChunk> chunks, String content, int chunkId, String documentId, String sourceName) {
        String cleanContent = content.trim();

        if (cleanContent.length() < 80) {
            return chunkId;
        }

        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(chunkId);
        chunk.setDocumentId(documentId);
        chunk.setSourceName(sourceName);
        chunk.setContent(cleanContent);
        chunk.setEmbedding(null);

        chunks.add(chunk);
        return chunkId + 1;
    }

    private String getOverlap(String text) {
        if (text == null || text.length() <= OVERLAP_CHARS) {
            return text == null ? "" : text;
        }

        return text.substring(text.length() - OVERLAP_CHARS);
    }
}
