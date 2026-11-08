package com.isga.chatbot.model.admin;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// Collection MongoDB pour les documents ajoutés par l'admin
@Document(collection = "admin_documents")
public class AdminDocument {

    // Id MongoDB
    @Id
    private String id;

    // Nom original du fichier
    private String originalFileName;

    // Nom du fichier stocké dans le serveur
    private String storedFileName;

    // Type du fichier : pdf, docx, txt
    private String fileType;

    // Chemin local du fichier
    private String filePath;

    // Date d'ajout
    private LocalDateTime uploadedAt;

    // Est-ce que le document est indexé dans le RAG ?
    private boolean indexed;

    public AdminDocument() {
    }

    public AdminDocument(String originalFileName, String storedFileName, String fileType, String filePath) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileType = fileType;
        this.filePath = filePath;
        this.uploadedAt = LocalDateTime.now();
        this.indexed = false;
    }

    public String getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }
}