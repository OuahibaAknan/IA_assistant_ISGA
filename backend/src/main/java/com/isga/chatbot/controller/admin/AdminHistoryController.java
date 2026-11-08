package com.isga.chatbot.controller.admin;

import com.isga.chatbot.model.admin.QuestionHistory;
import com.isga.chatbot.service.admin.AdminHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/history")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminHistoryController {

    @Autowired
    private AdminHistoryService historyService;

    @GetMapping
    public List<QuestionHistory> getAllHistory() {
        return historyService.getAllHistory();
    }

    @GetMapping("/last")
    public List<QuestionHistory> getLastQuestions() {
        return historyService.getLastQuestions();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearHistory() {
        historyService.clearHistory();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Historique supprimé"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHistoryById(@PathVariable String id) {
        historyService.deleteHistoryById(id);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Question supprimée"
        ));
    }

    @PostMapping("/export/pdf/selected")
    public ResponseEntity<ByteArrayResource> exportSelectedPdf(
            @RequestBody Map<String, List<String>> body
    ) throws Exception {

        List<String> ids = body.get("ids");

        byte[] data = historyService.exportHistoryPdfByIds(ids);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historique-selection-isga.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(data.length)
                .body(resource);
    }

    @PostMapping("/export/word/selected")
    public ResponseEntity<ByteArrayResource> exportSelectedWord(
            @RequestBody Map<String, List<String>> body
    ) {

        List<String> ids = body.get("ids");

        byte[] data = historyService.exportHistoryWordByIds(ids);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historique-selection-isga.doc")
                .contentType(MediaType.parseMediaType("application/msword"))
                .contentLength(data.length)
                .body(resource);
    }
}