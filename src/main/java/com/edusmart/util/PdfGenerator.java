package com.edusmart.util;

import com.edusmart.model.Bulletin;
import com.edusmart.model.Certification;
import com.edusmart.model.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Generates professional EduSmart-branded PDF documents for Bulletins and Certifications.
 *
 * <p>Color palette (mirrors the JavaFX app CSS):
 * <ul>
 *   <li>Primary blue  : #1E3A8A</li>
 *   <li>Secondary blue: #2563EB</li>
 *   <li>Purple accent : #7C3AED</li>
 *   <li>Background    : #F8FAFC</li>
 *   <li>Muted text    : #64748B</li>
 *   <li>Border        : #E2E8F0</li>
 *   <li>Success green : #10B981</li>
 * </ul>
 */
public class PdfGenerator {

    // ── EduSmart brand colours ──────────────────────────────────────────────
    private static final Color EDU_BLUE        = hex("#1E3A8A");
    private static final Color EDU_BLUE_LIGHT  = hex("#2563EB");
    private static final Color EDU_PURPLE      = hex("#7C3AED");
    private static final Color EDU_BG          = hex("#F8FAFC");
    private static final Color EDU_TEXT        = hex("#1E293B");
    private static final Color EDU_TEXT_MUTED  = hex("#64748B");
    private static final Color EDU_BORDER      = hex("#E2E8F0");
    private static final Color EDU_SUCCESS     = hex("#10B981");
    private static final Color EDU_WHITE       = Color.WHITE;

    private static final String PDF_DIR = "generated_pdfs";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    static {
        try {
            Files.createDirectories(Paths.get(PDF_DIR));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════

    /** Generates a professional Bulletin PDF and returns the generated file. */
    public static File generateBulletinPdf(Bulletin bulletin, User student) throws Exception {
        String safeLastName = student.getLastName() != null
                ? student.getLastName().replaceAll("[^a-zA-Z0-9_-]", "") : "etudiant";
        String safeYear = bulletin.getAcademicYear() != null
                ? bulletin.getAcademicYear().replaceAll("[^a-zA-Z0-9_-]", "") : "annee";
        String fileName = "Bulletin_" + safeLastName + "_" + safeYear + "_" + nvl(bulletin.getSemester(), "S") + ".pdf";
        File file = new File(PDF_DIR, fileName);

        Document doc = new Document(PageSize.A4, 50, 50, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        drawPageBackground(writer, doc, false);
        addBulletinHeader(doc, writer, bulletin);
        addSectionTitle(doc, "Informations de l'étudiant");
        addInfoTable(doc, new String[][]{
            {"Nom complet", student.getFullName()},
            {"Email",       nvl(student.getEmail(), "N/A")},
            {"Année",       nvl(bulletin.getAcademicYear(), "N/A")},
            {"Semestre",    nvl(bulletin.getSemester(), "N/A")},
            {"Statut",      nvl(bulletin.getStatus(), "N/A")}
        });
        addSectionTitle(doc, "Résultats académiques");
        addResultsTable(doc, new String[][]{
            {"Moyenne générale", bulletin.getAverage() != null ? String.format("%.2f / 20", bulletin.getAverage()) : "N/A"},
            {"Mention",          nvl(bulletin.getMention(), "N/A")},
            {"Classement",       bulletin.getClassRank() != null ? bulletin.getClassRank() + "ème" : "N/A"}
        });
        addQrSection(doc, "https://edusmart.com/bulletin/" + bulletin.getId(),
                "Bulletin #" + bulletin.getId() + " — Vérifiez l'authenticité sur edusmart.com");
        drawBottomStrip(writer, doc);

        doc.close();
        return file;
    }

    /** Generates a professional Certification PDF and returns the generated file. */
    public static File generateCertificationPdf(Certification cert, User student) throws Exception {
        String uniqueNum = nvl(cert.getUniqueNumber(), String.valueOf(cert.getId()));
        String fileName  = "Certification_" + uniqueNum.replaceAll("[^a-zA-Z0-9_-]", "") + ".pdf";
        File file        = new File(PDF_DIR, fileName);

        Document doc = new Document(PageSize.A4.rotate(), 60, 60, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        drawCertDecorations(writer, doc);
        addCertContent(doc, writer, cert, student, uniqueNum);

        doc.close();
        return file;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BULLETIN HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private static void drawPageBackground(PdfWriter writer, Document doc, boolean cert) {
        PdfContentByte canvas = writer.getDirectContentUnder();
        Rectangle page = doc.getPageSize();
        canvas.setColorFill(EDU_BG);
        canvas.rectangle(0, 0, page.getWidth(), page.getHeight());
        canvas.fill();
    }

    private static void addBulletinHeader(Document doc, PdfWriter writer, Bulletin bulletin) throws Exception {
        Rectangle page = doc.getPageSize();
        float bannerH = 90;

        PdfContentByte canvas = writer.getDirectContent();
        // Blue top banner
        canvas.setColorFill(EDU_BLUE);
        canvas.rectangle(0, page.getHeight() - bannerH, page.getWidth(), bannerH);
        canvas.fill();
        // Purple accent strip under banner
        canvas.setColorFill(EDU_PURPLE);
        canvas.rectangle(0, page.getHeight() - bannerH - 5, page.getWidth(), 5);
        canvas.fill();

        BaseFont bf = boldFont();
        canvas.beginText();
        canvas.setFontAndSize(bf, 22);
        canvas.setColorFill(EDU_WHITE);
        canvas.showTextAligned(Element.ALIGN_LEFT, "EduSmart", 50, page.getHeight() - 38, 0);
        canvas.endText();

        BaseFont bfR = regularFont();
        canvas.beginText();
        canvas.setFontAndSize(bfR, 10);
        canvas.setColorFill(new Color(220, 230, 255));
        canvas.showTextAligned(Element.ALIGN_LEFT, "Plateforme Éducative Intelligente", 50, page.getHeight() - 55, 0);
        canvas.endText();

        // Spacer for banner
        doc.add(blankLines(3));

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, EDU_BLUE);
        Paragraph title = new Paragraph("BULLETIN DE NOTES", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10);
        doc.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11, EDU_TEXT_MUTED);
        Paragraph sub = new Paragraph("Année académique · " + nvl(bulletin.getAcademicYear(), "N/A"), subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(14);
        doc.add(sub);

        addHRule(doc);
    }

    private static void addSectionTitle(Document doc, String text) throws Exception {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, EDU_BLUE);
        Paragraph p = new Paragraph(text, f);
        p.setSpacingBefore(16);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private static void addInfoTable(Document doc, String[][] rows) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40f, 60f});
        table.setSpacingAfter(8);

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, EDU_TEXT);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, EDU_TEXT_MUTED);

        for (int i = 0; i < rows.length; i++) {
            Color bg = (i % 2 == 0) ? EDU_WHITE : EDU_BG;
            PdfPCell lc = styledCell(new Phrase(rows[i][0], labelFont), bg, Element.ALIGN_LEFT);
            PdfPCell vc = styledCell(new Phrase(rows[i][1], valueFont), bg, Element.ALIGN_RIGHT);
            table.addCell(lc);
            table.addCell(vc);
        }
        doc.add(table);
    }

    private static void addResultsTable(Document doc, String[][] rows) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50f, 50f});
        table.setSpacingAfter(12);

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, EDU_WHITE);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, EDU_WHITE);

        for (String[] row : rows) {
            PdfPCell lc = solidCell(new Phrase(row[0], labelFont), EDU_BLUE, Element.ALIGN_LEFT);
            PdfPCell vc = solidCell(new Phrase(row[1], valueFont), EDU_BLUE_LIGHT, Element.ALIGN_RIGHT);
            table.addCell(lc);
            table.addCell(vc);
        }
        doc.add(table);
    }

    private static void addQrSection(Document doc, String qrData, String footerText) throws Exception {
        doc.add(blankLines(1));

        com.lowagie.text.Image qr = qrCodeImage(qrData, 90, 90);
        qr.setAlignment(Element.ALIGN_CENTER);
        doc.add(qr);

        Font f = FontFactory.getFont(FontFactory.HELVETICA, 9, EDU_TEXT_MUTED);
        Paragraph p = new Paragraph(footerText, f);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(4);
        doc.add(p);

        addHRule(doc);
    }

    private static void drawBottomStrip(PdfWriter writer, Document doc) {
        PdfContentByte canvas = writer.getDirectContent();
        canvas.setColorFill(EDU_BLUE);
        canvas.rectangle(0, 0, doc.getPageSize().getWidth(), 20);
        canvas.fill();
        canvas.setColorFill(EDU_PURPLE);
        canvas.rectangle(0, 20, doc.getPageSize().getWidth(), 4);
        canvas.fill();
    }

    private static void addHRule(Document doc) throws Exception {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorderWidth(0);
        cell.setBorderWidthBottom(1);
        cell.setBorderColorBottom(EDU_BORDER);
        cell.setFixedHeight(1);
        cell.setPaddingBottom(0);
        line.addCell(cell);
        doc.add(line);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CERTIFICATION HELPERS (landscape A4)
    // ═══════════════════════════════════════════════════════════════════════

    private static void drawCertDecorations(PdfWriter writer, Document doc) {
        PdfContentByte c = writer.getDirectContentUnder();
        Rectangle page = doc.getPageSize();
        float w = page.getWidth(), h = page.getHeight();

        // White fill
        c.setColorFill(EDU_WHITE);
        c.rectangle(0, 0, w, h);
        c.fill();

        // Left blue bar
        c.setColorFill(EDU_BLUE);
        c.rectangle(0, 0, 18, h);
        c.fill();

        // Right purple bar
        c.setColorFill(EDU_PURPLE);
        c.rectangle(w - 18, 0, 18, h);
        c.fill();

        // Top strip
        c.setColorFill(EDU_BLUE);
        c.rectangle(18, h - 14, w - 36, 14);
        c.fill();

        // Bottom strip
        c.setColorFill(EDU_BLUE);
        c.rectangle(18, 0, w - 36, 14);
        c.fill();

        // Inner border
        c.setColorStroke(EDU_BORDER);
        c.setLineWidth(1.5f);
        c.rectangle(32, 28, w - 64, h - 56);
        c.stroke();
    }

    private static void addCertContent(Document doc, PdfWriter writer,
                                        Certification cert, User student,
                                        String uniqueNum) throws Exception {
        Rectangle page = doc.getPageSize();
        PdfContentByte canvas = writer.getDirectContent();

        // Logo header (absolute)
        canvas.beginText();
        canvas.setFontAndSize(boldFont(), 28);
        canvas.setColorFill(EDU_BLUE);
        canvas.showTextAligned(Element.ALIGN_CENTER, "EduSmart", page.getWidth() / 2, page.getHeight() - 72, 0);
        canvas.endText();

        canvas.beginText();
        canvas.setFontAndSize(regularFont(), 11);
        canvas.setColorFill(EDU_TEXT_MUTED);
        canvas.showTextAligned(Element.ALIGN_CENTER, "Plateforme Éducative Intelligente",
                page.getWidth() / 2, page.getHeight() - 91, 0);
        canvas.endText();

        canvas.setColorStroke(EDU_PURPLE);
        canvas.setLineWidth(2f);
        float cx = page.getWidth() / 2;
        canvas.moveTo(cx - 100, page.getHeight() - 100);
        canvas.lineTo(cx + 100, page.getHeight() - 100);
        canvas.stroke();

        // Flow content — spacer for logo
        doc.add(blankLines(5));

        Font certTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, EDU_BLUE);
        Paragraph certTitle = new Paragraph("CERTIFICAT DE RÉUSSITE", certTitleFont);
        certTitle.setAlignment(Element.ALIGN_CENTER);
        certTitle.setSpacingBefore(8);
        doc.add(certTitle);

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 14, EDU_TEXT_MUTED);
        Paragraph intro = new Paragraph("Ce certificat est décerné à", normalFont);
        intro.setAlignment(Element.ALIGN_CENTER);
        intro.setSpacingBefore(22);
        doc.add(intro);

        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, EDU_BLUE);
        Paragraph name = new Paragraph(student.getFullName(), nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        name.setSpacingBefore(6);
        name.setSpacingAfter(6);
        doc.add(name);

        Paragraph forText = new Paragraph("pour avoir validé avec succès la formation de", normalFont);
        forText.setAlignment(Element.ALIGN_CENTER);
        doc.add(forText);

        Font typeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, EDU_PURPLE);
        Paragraph type = new Paragraph(nvl(cert.getCertificationType(), "Formation EduSmart"), typeFont);
        type.setAlignment(Element.ALIGN_CENTER);
        type.setSpacingBefore(8);
        doc.add(type);

        // Dates
        if (cert.getIssuedAt() != null || cert.getValidUntil() != null) {
            doc.add(blankLines(1));
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12, EDU_TEXT_MUTED);
            StringBuilder sb = new StringBuilder();
            if (cert.getIssuedAt()  != null) sb.append("Délivré le : ").append(cert.getIssuedAt().format(FMT));
            if (cert.getValidUntil() != null) sb.append("   |   Valide jusqu'au : ").append(cert.getValidUntil().format(FMT));
            Paragraph dates = new Paragraph(sb.toString(), dateFont);
            dates.setAlignment(Element.ALIGN_CENTER);
            doc.add(dates);
        }

        // QR Code
        doc.add(blankLines(1));
        com.lowagie.text.Image qr = qrCodeImage("https://edusmart.com/verify/" + uniqueNum, 85, 85);
        qr.setAlignment(Element.ALIGN_CENTER);
        doc.add(qr);

        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, EDU_TEXT_MUTED);
        Paragraph verif = new Paragraph("N° : " + uniqueNum
                + "   |   Scannez le QR code pour vérifier l'authenticité sur edusmart.com",
                smallFont);
        verif.setAlignment(Element.ALIGN_CENTER);
        verif.setSpacingBefore(4);
        doc.add(verif);

        // Official seal (absolute)
        float sealCx = page.getWidth() - 110, sealCy = 70, r = 38;
        canvas.setColorStroke(EDU_PURPLE);
        canvas.setLineWidth(1.5f);
        canvas.circle(sealCx, sealCy, r);
        canvas.stroke();
        canvas.beginText();
        canvas.setFontAndSize(boldFont(), 7);
        canvas.setColorFill(EDU_PURPLE);
        canvas.showTextAligned(Element.ALIGN_CENTER, "OFFICIEL", sealCx, sealCy + 6, 0);
        canvas.showTextAligned(Element.ALIGN_CENTER, "EduSmart", sealCx, sealCy - 6, 0);
        canvas.endText();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CELL FACTORIES
    // ═══════════════════════════════════════════════════════════════════════

    private static PdfPCell styledCell(Phrase content, Color bg, int align) {
        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bg);
        cell.setBorderWidth(0);
        cell.setBorderWidthBottom(0.5f);
        cell.setBorderColorBottom(EDU_BORDER);
        cell.setPadding(8);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private static PdfPCell solidCell(Phrase content, Color bg, int align) {
        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bg);
        cell.setBorderWidth(0);
        cell.setBorderWidthBottom(2);
        cell.setBorderColorBottom(EDU_WHITE);
        cell.setPadding(9);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private static com.lowagie.text.Image qrCodeImage(String data, int w, int h) throws Exception {
        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix bm = qrWriter.encode(data, BarcodeFormat.QR_CODE, w, h);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bm, "PNG", out);
        return com.lowagie.text.Image.getInstance(out.toByteArray());
    }

    private static BaseFont regularFont() throws Exception {
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
    }

    private static BaseFont boldFont() throws Exception {
        return BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);
    }

    private static Paragraph blankLines(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append('\n');
        return new Paragraph(sb.toString());
    }

    private static Color hex(String h) {
        return Color.decode(h);
    }

    private static String nvl(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }
}
