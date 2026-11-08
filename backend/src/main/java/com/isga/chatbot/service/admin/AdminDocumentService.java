package com.isga.chatbot.service.admin;

import com.isga.chatbot.model.admin.AdminDocument;
import com.isga.chatbot.model.rag.DocumentChunk;
import com.isga.chatbot.repository.admin.AdminDocumentRepository;
import com.isga.chatbot.service.rag.ChunkService;
import com.isga.chatbot.service.rag.EmbeddingService;
import com.isga.chatbot.service.rag.VectorStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDocumentService {

    @Autowired
    private AdminFileStorageService fileStorageService;

    @Autowired
    private DocumentReaderService documentReaderService;

    @Autowired
    private AdminDocumentRepository documentRepository;

    @Autowired
    private ChunkService chunkService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorStoreService vectorStoreService;

    public AdminDocument uploadDocument(MultipartFile file) throws Exception {
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new RuntimeException("Nom de fichier invalide");
        }

        if (!originalFileName.contains(".")) {
            throw new RuntimeException("Le fichier doit avoir une extension");
        }

        String extension = originalFileName
                .substring(originalFileName.lastIndexOf(".") + 1)
                .toLowerCase();

        if (!extension.equals("pdf") && !extension.equals("docx") && !extension.equals("txt")) {
            throw new RuntimeException("Format non supporté. Utilisez PDF, DOCX ou TXT.");
        }

        String storedFileName = fileStorageService.saveFile(file);
        String filePath = fileStorageService.getFilePath(storedFileName);

        AdminDocument document = new AdminDocument(
                originalFileName,
                storedFileName,
                extension,
                filePath
        );

        AdminDocument savedDocument = documentRepository.save(document);

        // Important : dès qu'un admin ajoute un document, on reconstruit le RAG automatiquement.
        reindexAllDocuments();

        return documentRepository.findById(savedDocument.getId()).orElse(savedDocument);
    }

    public List<AdminDocument> getAllDocuments() {
        return documentRepository.findAll();
    }

    public void deleteDocument(String id) throws Exception {
        AdminDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));

        fileStorageService.deleteFile(document.getStoredFileName());
        documentRepository.deleteById(id);

        // Important : après suppression, le RAG ne doit plus utiliser ce document.
        reindexAllDocuments();
    }

    public int reindexAllDocuments() throws Exception {
        List<AdminDocument> documents = documentRepository.findAll();
        List<DocumentChunk> allChunks = new ArrayList<>();
        int globalId = 0;

        for (AdminDocument document : documents) {
            String text = documentReaderService.readFile(
                    document.getFilePath(),
                    document.getFileType()
            );

            List<DocumentChunk> chunks = chunkService.splitTextIntoChunks(
                    text,
                    document.getId(),
                    document.getOriginalFileName()
            );

            for (DocumentChunk chunk : chunks) {
                chunk.setId(globalId++);
                chunk.setEmbedding(embeddingService.createEmbedding(chunk.getContent()));
                allChunks.add(chunk);
            }

            document.setIndexed(true);
            documentRepository.save(document);
        }

        vectorStoreService.saveChunks(allChunks);
        System.out.println("✅ RAG dashboard réindexé avec " + allChunks.size() + " chunks");
        return allChunks.size();
    }
}
