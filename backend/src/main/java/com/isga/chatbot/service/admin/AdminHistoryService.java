package com.isga.chatbot.service.admin;

import com.isga.chatbot.model.admin.QuestionHistory;
import com.isga.chatbot.repository.admin.QuestionHistoryRepository;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class AdminHistoryService {

    @Autowired
    private QuestionHistoryRepository historyRepository;

    public QuestionHistory saveQuestion(
            String question,
            String answer,
            String status,
            long responseTimeMs,
            List<String> sourceDocuments,
            int usedChunks,
            double averageScore
    ) {
        QuestionHistory history = new QuestionHistory(
                question,
                answer,
                status,
                responseTimeMs,
                sourceDocuments,
                usedChunks,
                averageScore
        );

        return historyRepository.save(history);
    }

    public List<QuestionHistory> getAllHistory() {
        return historyRepository.findAll();
    }

    public List<QuestionHistory> getLastQuestions() {
        return historyRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public void clearHistory() {
        historyRepository.deleteAll();
    }

    public void deleteHistoryById(String id) {
        if (!historyRepository.existsById(id)) {
            throw new RuntimeException("Question introuvable");
        }

        historyRepository.deleteById(id);
    }

    public byte[] exportHistoryExcel() throws Exception {
        List<QuestionHistory> history = historyRepository.findAll();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Historique IA");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Question");
        header.createCell(1).setCellValue("Réponse");
        header.createCell(2).setCellValue("Statut");
        header.createCell(3).setCellValue("Temps IA");
        header.createCell(4).setCellValue("Score moyen");
        header.createCell(5).setCellValue("Chunks utilisés");
        header.createCell(6).setCellValue("Documents sources");
        header.createCell(7).setCellValue("Date");

        int rowNum = 1;

        for (QuestionHistory item : history) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(safe(item.getQuestion()));
            row.createCell(1).setCellValue(safe(item.getAnswer()));
            row.createCell(2).setCellValue(safe(item.getStatus()));
            row.createCell(3).setCellValue(item.getResponseTimeMs());
            row.createCell(4).setCellValue(item.getAverageScore());
            row.createCell(5).setCellValue(item.getUsedChunks());
            row.createCell(6).setCellValue(
                    item.getSourceDocuments() == null ? "" : String.join(", ", item.getSourceDocuments())
            );
            row.createCell(7).setCellValue(item.getCreatedAt() == null ? "" : item.getCreatedAt().toString());
        }

        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    public byte[] exportHistoryPdf() throws Exception {
        List<QuestionHistory> history = historyRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document pdf = new Document();
        PdfWriter.getInstance(pdf, out);

        pdf.open();

        pdf.add(new Paragraph("Historique IA - Chatbot ISGA"));
        pdf.add(new Paragraph(" "));

        for (QuestionHistory item : history) {
            pdf.add(new Paragraph("Question : " + safe(item.getQuestion())));
            pdf.add(new Paragraph("Réponse : " + safe(item.getAnswer())));
            pdf.add(new Paragraph("Statut : " + safe(item.getStatus())));
            pdf.add(new Paragraph("Temps IA : " + item.getResponseTimeMs() + " ms"));
            pdf.add(new Paragraph("Score moyen : " + item.getAverageScore()));
            pdf.add(new Paragraph("Chunks utilisés : " + item.getUsedChunks()));
            pdf.add(new Paragraph("Sources : " + (
                    item.getSourceDocuments() == null ? "" : String.join(", ", item.getSourceDocuments())
            )));
            pdf.add(new Paragraph("Date : " + (item.getCreatedAt() == null ? "" : item.getCreatedAt())));
            pdf.add(new Paragraph("------------------------------------------------------------"));
        }

        pdf.close();

        return out.toByteArray();
    }

    public byte[] exportHistoryWord() {
        List<QuestionHistory> history = historyRepository.findAll();

        StringBuilder html = new StringBuilder();

        html.append("""
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; color: #111; }
                        h1 { color: #b3072f; }
                        .card { border: 1px solid #ddd; padding: 14px; margin-bottom: 16px; }
                        .label { font-weight: bold; color: #b3072f; }
                    </style>
                </head>
                <body>
                <h1>Historique IA - Chatbot ISGA</h1>
                """);

        for (QuestionHistory item : history) {
            html.append("<div class='card'>")
                    .append("<p><span class='label'>Question :</span> ")
                    .append(escapeHtml(item.getQuestion()))
                    .append("</p>")

                    .append("<p><span class='label'>Réponse :</span> ")
                    .append(escapeHtml(item.getAnswer()))
                    .append("</p>")

                    .append("<p><span class='label'>Statut :</span> ")
                    .append(escapeHtml(item.getStatus()))
                    .append("</p>")

                    .append("<p><span class='label'>Temps IA :</span> ")
                    .append(item.getResponseTimeMs())
                    .append(" ms</p>")

                    .append("<p><span class='label'>Score moyen :</span> ")
                    .append(item.getAverageScore())
                    .append("</p>")

                    .append("<p><span class='label'>Date :</span> ")
                    .append(item.getCreatedAt() == null ? "" : item.getCreatedAt())
                    .append("</p>")

                    .append("</div>");
        }

        html.append("</body></html>");

        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {
        if (value == null) return "";

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
    public byte[] exportHistoryPdfByIds(List<String> ids) throws Exception {

        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("Aucune question sélectionnée");
        }

        List<QuestionHistory> history = historyRepository.findAllById(ids);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document pdf = new Document();
        PdfWriter.getInstance(pdf, out);

        pdf.open();

        pdf.add(new Paragraph("Historique sélectionné - Chatbot ISGA"));
        pdf.add(new Paragraph(" "));

        for (QuestionHistory item : history) {
            pdf.add(new Paragraph("Question : " + safe(item.getQuestion())));
            pdf.add(new Paragraph("Réponse : " + safe(item.getAnswer())));
            pdf.add(new Paragraph("Statut : " + safe(item.getStatus())));
            pdf.add(new Paragraph("Temps IA : " + item.getResponseTimeMs() + " ms"));
            pdf.add(new Paragraph("Score moyen : " + item.getAverageScore()));
            pdf.add(new Paragraph("Date : " + (item.getCreatedAt() == null ? "" : item.getCreatedAt())));
            pdf.add(new Paragraph("------------------------------------------------------------"));
        }

        pdf.close();

        return out.toByteArray();
    }

    public byte[] exportHistoryWordByIds(List<String> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("Aucune question sélectionnée");
        }

        List<QuestionHistory> history = historyRepository.findAllById(ids);

        StringBuilder html = new StringBuilder();

        html.append("""
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; color: #111; }
                    h1 { color: #b3072f; }
                    .card { border: 1px solid #ddd; padding: 14px; margin-bottom: 16px; }
                    .label { font-weight: bold; color: #b3072f; }
                </style>
            </head>
            <body>
            <h1>Historique sélectionné - Chatbot ISGA</h1>
            """);

        for (QuestionHistory item : history) {
            html.append("<div class='card'>")
                    .append("<p><span class='label'>Question :</span> ")
                    .append(escapeHtml(item.getQuestion()))
                    .append("</p>")

                    .append("<p><span class='label'>Réponse :</span> ")
                    .append(escapeHtml(item.getAnswer()))
                    .append("</p>")

                    .append("<p><span class='label'>Statut :</span> ")
                    .append(escapeHtml(item.getStatus()))
                    .append("</p>")

                    .append("<p><span class='label'>Temps IA :</span> ")
                    .append(item.getResponseTimeMs())
                    .append(" ms</p>")

                    .append("<p><span class='label'>Date :</span> ")
                    .append(item.getCreatedAt() == null ? "" : item.getCreatedAt())
                    .append("</p>")

                    .append("</div>");
        }

        html.append("</body></html>");

        return html.toString().getBytes(StandardCharsets.UTF_8);
    }
}