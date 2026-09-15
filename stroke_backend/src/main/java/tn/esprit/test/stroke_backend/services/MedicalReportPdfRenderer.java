package tn.esprit.test.stroke_backend.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.springframework.stereotype.Service;

@Service
public class MedicalReportPdfRenderer {

    private static final PDRectangle PAGE_SIZE =
            PDRectangle.A4;

    private static final float PAGE_WIDTH =
            PAGE_SIZE.getWidth();

    private static final float PAGE_HEIGHT =
            PAGE_SIZE.getHeight();

    private static final float MARGIN_LEFT = 55f;
    private static final float MARGIN_RIGHT = 55f;
    private static final float MARGIN_TOP = 30f;
    private static final float MARGIN_BOTTOM = 55f;

    private static final float CONTENT_WIDTH =
            PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    private static final float LINE_HEIGHT = 13f;

    private static final PDColor BLACK =
            new PDColor(
                    new float[]{0f, 0f, 0f},
                    PDDeviceRGB.INSTANCE
            );

    private static final PDType1Font REGULAR =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private static final PDType1Font BOLD =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    private static final PDType1Font ITALIC =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_OBLIQUE
            );

    public byte[] render(
            MedicalReportContent content
    ) {

        if (content == null) {
            throw new IllegalArgumentException(
                    "Medical report content cannot be null"
            );
        }

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            PdfCursor cursor =
                    new PdfCursor(document, content);

            cursor.render();

            document.save(output);

            return output.toByteArray();

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to render medical report PDF",
                    exception
            );
        }
    }

    private class PdfCursor {

        private final PDDocument document;
        private final MedicalReportContent content;

        private PDPage page;
        private PDPageContentStream stream;

        private float y;
        private int pageNumber = 0;

        PdfCursor(
                PDDocument document,
                MedicalReportContent content
        ) {
            this.document = document;
            this.content = content;
        }

        void render() throws IOException {

            createPage();

            drawHeader();

            section("PATIENT");

            field("Code patient :", content.patientCode());
            field("Nom et prénom :", content.patientName());
            field("Date de naissance :", content.dateOfBirth());
            field("Âge :", content.age() + " ans");
            field("Sexe :", content.sex());

            section("EXAMEN");

            field("Code de l’étude :", content.studyCode());
            field("Date de l’examen :", content.studyDate());
            field("Type d’examen :", content.modality());

            section("INDICATION");

            paragraph(content.indication());

            section("TECHNIQUE");

            paragraph(content.technique());

            section("RÉSULTATS");

            paragraph(content.resultats());

            section("CONCLUSION");

            paragraph(content.conclusion());

            drawSignature();

            closePage();
        }

        private void createPage() throws IOException {

            page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            pageNumber++;

            stream = new PDPageContentStream(
                    document,
                    page
            );

            stream.setNonStrokingColor(BLACK);
            stream.setStrokingColor(BLACK);

            y = PAGE_HEIGHT - MARGIN_TOP;
        }

        private void closePage() throws IOException {

            drawFooter();

            stream.close();
        }

        private void drawHeader() throws IOException {

            centeredText(
                    BOLD,
                    13f,
                    "CENTRE D’IMAGERIE MÉDICALE"
            );

            y -= 16f;

            centeredText(
                    BOLD,
                    11f,
                    "SERVICE DE RADIOLOGIE"
            );

            y -= 14f;

            centeredText(
                    REGULAR,
                    9f,
                    "UNITÉ DE NEURO-IMAGERIE"
            );

            y -= 13f;

            centeredText(
                    REGULAR,
                    8f,
                    content.centerAddress()
            );

            y -= 12f;

            centeredText(
                    REGULAR,
                    8f,
                    content.centerPhone()
            );

            y -= 25f;

            centeredText(
                    BOLD,
                    12f,
                    "COMPTE RENDU D’IRM CÉRÉBRALE"
            );

            y -= 9f;

            centeredText(
                    REGULAR,
                    9f,
                    "Séquence de diffusion cérébrale"
            );

            y -= 22f;

            drawHorizontalLine();

            y -= 20f;
        }

        private void drawSignature() throws IOException {

            ensureSpace(80f);

            y -= 20f;

            centeredText(
                    BOLD,
                    9.5f,
                    content.doctorName()
            );

            y -= 13f;

            centeredText(
                    REGULAR,
                    9f,
                    content.doctorTitle()
            );

            y -= 13f;

            centeredText(
                    REGULAR,
                    8.5f,
                    "Signature : __________________________"
            );
        }

        private void drawFooter() throws IOException {

            float footerY = 38f;

            stream.setLineWidth(0.5f);

            stream.moveTo(
                    MARGIN_LEFT,
                    footerY + 12f
            );

            stream.lineTo(
                    PAGE_WIDTH - MARGIN_RIGHT,
                    footerY + 12f
            );

            stream.stroke();

            centeredTextAt(
                    REGULAR,
                    7.5f,
                    PAGE_WIDTH / 2f,
                    footerY,
                    "Document médical confidentiel — "
                            + "À conserver dans le dossier du patient"
            );

            drawTextAt(
                    REGULAR,
                    7.5f,
                    PAGE_WIDTH - MARGIN_RIGHT - 35f,
                    footerY - 14f,
                    "Page " + pageNumber
            );
        }

        private void section(String title) throws IOException {

            ensureSpace(35f);

            String sectionTitle =
                    sanitize(title.toUpperCase());

            drawText(
                    BOLD,
                    10f,
                    MARGIN_LEFT,
                    y,
                    sectionTitle
            );

            float width =
                    BOLD.getStringWidth(sectionTitle)
                            / 1000f
                            * 10f;

            stream.setLineWidth(0.5f);

            stream.moveTo(
                    MARGIN_LEFT,
                    y - 2f
            );

            stream.lineTo(
                    MARGIN_LEFT + width,
                    y - 2f
            );

            stream.stroke();

            y -= 19f;
        }

        private void field(
                String label,
                String value
        ) throws IOException {

            ensureSpace(20f);

            String text =
                    sanitize(label + " " + safe(value));

            drawText(
                    REGULAR,
                    9.5f,
                    MARGIN_LEFT,
                    y,
                    text
            );

            y -= 15f;
        }

        private void paragraph(String text)
                throws IOException {

            if (text == null || text.isBlank()) {
                return;
            }

            List<String> lines =
                    wrapText(
                            sanitize(text),
                            REGULAR,
                            9.5f,
                            CONTENT_WIDTH
                    );

            for (String line : lines) {

                ensureSpace(20f);

                drawText(
                        REGULAR,
                        9.5f,
                        MARGIN_LEFT,
                        y,
                        line
                );

                y -= LINE_HEIGHT;
            }

            y -= 8f;
        }

        private void centeredText(
                PDType1Font font,
                float fontSize,
                String text
        ) throws IOException {

            String value = sanitize(text);

            float width =
                    font.getStringWidth(value)
                            / 1000f
                            * fontSize;

            float x =
                    (PAGE_WIDTH - width) / 2f;

            drawText(
                    font,
                    fontSize,
                    x,
                    y,
                    value
            );
        }

        private void centeredTextAt(
                PDType1Font font,
                float fontSize,
                float centerX,
                float yPosition,
                String text
        ) throws IOException {

            String value = sanitize(text);

            float width =
                    font.getStringWidth(value)
                            / 1000f
                            * fontSize;

            drawTextAt(
                    font,
                    fontSize,
                    centerX - width / 2f,
                    yPosition,
                    value
            );
        }

        private void drawText(
                PDType1Font font,
                float fontSize,
                float x,
                float yPosition,
                String text
        ) throws IOException {

            drawTextAt(
                    font,
                    fontSize,
                    x,
                    yPosition,
                    text
            );
        }

        private void drawTextAt(
                PDType1Font font,
                float fontSize,
                float x,
                float yPosition,
                String text
        ) throws IOException {

            stream.beginText();

            stream.setFont(
                    font,
                    fontSize
            );

            stream.setNonStrokingColor(BLACK);

            stream.newLineAtOffset(
                    x,
                    yPosition
            );

            stream.showText(
                    sanitize(text)
            );

            stream.endText();
        }

        private void drawHorizontalLine()
                throws IOException {

            stream.setLineWidth(0.5f);

            stream.moveTo(
                    MARGIN_LEFT,
                    y
            );

            stream.lineTo(
                    PAGE_WIDTH - MARGIN_RIGHT,
                    y
            );

            stream.stroke();
        }

        private void ensureSpace(float requiredHeight)
                throws IOException {

            if (y - requiredHeight <= MARGIN_BOTTOM) {

                closePage();

                createPage();

                drawHeader();
            }
        }

        private List<String> wrapText(
                String text,
                PDType1Font font,
                float fontSize,
                float maxWidth
        ) throws IOException {

            List<String> lines =
                    new ArrayList<>();

            String[] words =
                    text.trim().split("\\s+");

            StringBuilder currentLine =
                    new StringBuilder();

            for (String word : words) {

                String candidate =
                        currentLine.isEmpty()
                                ? word
                                : currentLine + " " + word;

                float width =
                        font.getStringWidth(candidate)
                                / 1000f
                                * fontSize;

                if (width <= maxWidth) {

                    currentLine.setLength(0);
                    currentLine.append(candidate);

                } else {

                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                    }

                    currentLine.setLength(0);
                    currentLine.append(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }

            return lines;
        }
    }

    private String sanitize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("–", "-")
                .replace("—", "-")
                .replace("…", "...")
                .replace("œ", "oe")
                .replace("Œ", "OE")
                .replace("€", "EUR");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}