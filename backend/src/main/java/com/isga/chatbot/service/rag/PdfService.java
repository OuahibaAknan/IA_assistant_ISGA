package com.isga.chatbot.service.rag;

// Permet de charger un fichier depuis src/main/resources
import org.springframework.core.io.ClassPathResource;

// Permet de déclarer cette classe comme service Spring
import org.springframework.stereotype.Service;

// Classes PDFBox (version 2.x)
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

// Permet de lire le fichier
import java.io.InputStream;

// Gestion des erreurs
import java.io.IOException;

// Cette classe est détectée automatiquement par Spring
@Service
public class PdfService {

    // Méthode pour lire un PDF
    public String extractTextFromPdf(String fileName) throws IOException {

        // 1. On récupère le fichier depuis resources/documents
        ClassPathResource resource = new ClassPathResource("documents/" + fileName);

        // 2. On ouvre le fichier comme un flux
        InputStream inputStream = resource.getInputStream();

        // 3. On charge le PDF avec PDFBox (version 2.x)
        PDDocument document = PDDocument.load(inputStream);

        // 4. Outil pour extraire le texte
        PDFTextStripper stripper = new PDFTextStripper();

        // 5. Extraction du texte
        String text = stripper.getText(document);

        // 6. On ferme le document
        document.close();

        // 7. On ferme le flux
        inputStream.close();

        // 8. On retourne le texte
        return text;
    }
}