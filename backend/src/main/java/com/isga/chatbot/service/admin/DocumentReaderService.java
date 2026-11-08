package com.isga.chatbot.service.admin;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;

import java.nio.file.Files;

import java.util.List;

@Service
public class DocumentReaderService {

    /*
     * OCR Tesseract
     */
    private final ITesseract tesseract;

    public DocumentReaderService() {

        tesseract = new Tesseract();

        /*
         * IMPORTANT :
         * adapte le chemin si nécessaire
         */
        tesseract.setDatapath(
                "C:/Program Files/Tesseract-OCR/tessdata"
        );

        /*
         * Langues supportées
         */
        tesseract.setLanguage("fra+eng+ara");
    }

    /*
     * Lecture intelligente
     */
    public String readFile(
            String filePath,
            String fileType
    ) throws Exception {

        if ("pdf".equalsIgnoreCase(fileType)) {
            return readPdf(filePath);
        }

        if ("txt".equalsIgnoreCase(fileType)) {
            return readTxt(filePath);
        }

        if ("docx".equalsIgnoreCase(fileType)) {
            return readDocx(filePath);
        }

        throw new RuntimeException(
                "Format non supporté : "
                        + fileType
        );
    }

    /*
     * PDF intelligent :
     * texte normal OU OCR
     */
    private String readPdf(String filePath)
            throws Exception {

        PDDocument document =
                PDDocument.load(
                        new File(filePath)
                );

        PDFTextStripper stripper =
                new PDFTextStripper();

        String extractedText =
                stripper.getText(document);

        /*
         * Si PDF texte normal
         */
        if (extractedText != null &&
                extractedText.trim().length() > 120) {

            document.close();

            System.out.println(
                    "✅ PDF texte détecté : "
                            + filePath
            );

            return cleanText(extractedText);
        }

        /*
         * Sinon OCR
         */
        System.out.println(
                "⚠ PDF scanné détecté → OCR : "
                        + filePath
        );

        PDFRenderer renderer =
                new PDFRenderer(document);

        StringBuilder finalText =
                new StringBuilder();

        for (int page = 0;
             page < document.getNumberOfPages();
             page++) {

            BufferedImage image =
                    renderer.renderImageWithDPI(
                            page,
                            300
                    );

            /*
             * OCR image
             */
            String ocrText =
                    tesseract.doOCR(image);

            finalText
                    .append("\n")
                    .append(ocrText);
        }

        document.close();

        return cleanText(finalText.toString());
    }

    /*
     * TXT
     */
    private String readTxt(String filePath)
            throws Exception {

        return cleanText(
                Files.readString(
                        new File(filePath).toPath()
                )
        );
    }

    /*
     * DOCX
     */
    private String readDocx(String filePath)
            throws Exception {

        FileInputStream inputStream =
                new FileInputStream(filePath);

        XWPFDocument document =
                new XWPFDocument(inputStream);

        List<XWPFParagraph> paragraphs =
                document.getParagraphs();

        StringBuilder text =
                new StringBuilder();

        for (XWPFParagraph paragraph : paragraphs) {

            text.append(
                    paragraph.getText()
            ).append("\n");
        }

        document.close();
        inputStream.close();

        return cleanText(text.toString());
    }

    /*
     * Nettoyage texte IA
     */
    private String cleanText(String text) {

        if (text == null) {
            return "";
        }

        return text

                /*
                 * espaces multiples
                 */
                .replaceAll("\\s+", " ")

                /*
                 * lignes vides
                 */
                .replaceAll(
                        "(\\r?\\n\\s*){2,}",
                        "\n"
                )

                /*
                 * caractères spéciaux invisibles
                 */
                .replace("\u0000", "")

                .trim();
    }
}