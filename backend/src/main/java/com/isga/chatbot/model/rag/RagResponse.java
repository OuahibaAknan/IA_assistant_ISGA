package com.isga.chatbot.model.rag;

import java.util.ArrayList;
import java.util.List;

public class RagResponse {

    private String answer;
    private String status;
    private List<RagSource> sources = new ArrayList<>();

    public RagResponse() {
    }

    public RagResponse(String answer) {
        this.answer = answer;
        this.status = "success";
    }

    public RagResponse(String answer, String status, List<RagSource> sources) {
        this.answer = answer;
        this.status = status;
        this.sources = sources;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<RagSource> getSources() { return sources; }
    public void setSources(List<RagSource> sources) { this.sources = sources; }
}
