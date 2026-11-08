package com.isga.chatbot.controller.rag;

import com.isga.chatbot.model.rag.RagRequest;
import com.isga.chatbot.model.rag.RagResponse;
import com.isga.chatbot.service.rag.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "http://localhost:3000")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    public ResponseEntity<RagResponse> ask(@RequestBody RagRequest request) {

        return ResponseEntity.ok(
                ragService.ask(request.getQuestion(), request.getHistory())
        );
    }
}