package com.isga.chatbot.service.admin;

// Permet de créer des noms de fichiers uniques
import java.util.UUID;

// Permet de manipuler les chemins de fichiers
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

// Permet de gérer les erreurs d'entrée/sortie
import java.io.IOException;

// Fichier reçu depuis une requête HTTP
import org.springframework.web.multipart.MultipartFile;

// Déclare cette classe comme service Spring
import org.springframework.stereotype.Service;

@Service
public class AdminFileStorageService {

    // Dossier où les fichiers seront stockés
    private final String uploadDir = "uploaded-documents";

    // Méthode qui sauvegarde un fichier reçu depuis React/Postman
    public String saveFile(MultipartFile file) throws IOException {

        // Crée le dossier uploaded-documents s'il n'existe pas
        Files.createDirectories(Paths.get(uploadDir));

        // Récupère le nom original du fichier
        String originalFileName = file.getOriginalFilename();

        // Récupère l'extension du fichier : pdf, docx, txt
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        // Crée un nom unique pour éviter les conflits
        String storedFileName = UUID.randomUUID() + extension;

        // Chemin final du fichier
        Path destination = Paths.get(uploadDir, storedFileName);

        // Copie le fichier dans uploaded-documents
        Files.copy(file.getInputStream(), destination);

        // Retourne le nom stocké
        return storedFileName;
    }

    // Méthode qui supprime un fichier localement
    public void deleteFile(String storedFileName) throws IOException {

        // Chemin du fichier à supprimer
        Path filePath = Paths.get(uploadDir, storedFileName);

        // Supprime le fichier seulement s'il existe
        Files.deleteIfExists(filePath);
    }

    // Retourne le chemin complet d'un fichier stocké
    public String getFilePath(String storedFileName) {

        // Exemple : uploaded-documents/abc.pdf
        return Paths.get(uploadDir, storedFileName).toString();
    }
}