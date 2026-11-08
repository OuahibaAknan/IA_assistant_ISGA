package com.isga.chatbot.repository.rag;

import com.isga.chatbot.model.rag.DocumentChunk;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RagChunkRepository extends MongoRepository<DocumentChunk, String> {
}
