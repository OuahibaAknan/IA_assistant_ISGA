package com.isga.chatbot.repository;

import com.isga.chatbot.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // MongoRepository fournit automatiquement save(), findAll(), count(), etc.
}