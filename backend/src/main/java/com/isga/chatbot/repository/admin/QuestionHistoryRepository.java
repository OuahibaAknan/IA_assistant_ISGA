package com.isga.chatbot.repository.admin;

// Repository MongoDB
import org.springframework.data.mongodb.repository.MongoRepository;

// Modèle historique
import com.isga.chatbot.model.admin.QuestionHistory;

// List Java
import java.util.List;

// Interface repository pour MongoDB
public interface QuestionHistoryRepository extends MongoRepository<QuestionHistory, String> {

    // Récupérer les 10 dernières questions triées par date décroissante
    List<QuestionHistory> findTop10ByOrderByCreatedAtDesc();
}