package com.isga.chatbot.repository.admin;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.isga.chatbot.model.admin.AdminDocument;

// Repository pour gérer les documents ajoutés par l'admin
public interface AdminDocumentRepository extends MongoRepository<AdminDocument, String> {

}