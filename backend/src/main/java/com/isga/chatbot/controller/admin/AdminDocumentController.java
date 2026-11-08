package com.isga.chatbot.controller.admin;

// Modèle document
import com.isga.chatbot.model.admin.AdminDocument;

// Service document admin
import com.isga.chatbot.service.admin.AdminDocumentService;

// Imports Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// List Java
import java.util.List;

// Map Java
import java.util.Map;

// Cette annotation dit que cette classe est un controller REST
@RestController

// Tous les endpoints commencent par /api/admin/documents
@RequestMapping("/api/admin/documents")

// Autoriser les appels depuis React
@CrossOrigin(origins = "http://localhost:3000")
public class AdminDocumentController {

    // Injection du service
    @Autowired
    private AdminDocumentService documentService;

    // Endpoint pour uploader un fichier
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {

        try {
            // Appeler le service pour sauvegarder le document
            AdminDocument savedDocument = documentService.uploadDocument(file);

            // Retourner le document sauvegardé
            return ResponseEntity.ok(savedDocument);

        } catch (Exception e) {

            // En cas d'erreur, retourner un message clair
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // Endpoint pour lister les documents
    @GetMapping
    public List<AdminDocument> getDocuments() {

        // Retourner tous les documents
        return documentService.getAllDocuments();
    }

    // Endpoint pour supprimer un document
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable String id) {

        try {
            // Appeler le service pour supprimer
            documentService.deleteDocument(id);

            // Réponse OK
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Document supprimé avec succès"
            ));

        } catch (Exception e) {

            // Réponse erreur
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // Endpoint pour réindexer tous les documents dans le RAG
    @PostMapping("/reindex")
    public ResponseEntity<?> reindexDocuments() {

        try {
            // Lancer la reindexation
            int chunksCount = documentService.reindexAllDocuments();

            // Réponse OK
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Documents réindexés avec succès",
                    "chunksCount", chunksCount
            ));

        } catch (Exception e) {

            // Réponse erreur
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}