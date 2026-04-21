package com.edusmart.util;

import com.edusmart.model.Bulletin;
import com.edusmart.model.Certification;
import com.edusmart.model.User;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating PDF documents for Bulletins and Certifications.
 */
public class PdfGenerator {

    private static final String PDF_DIR = "generated_pdfs";

    static {
        try {
            Files.createDirectories(Paths.get(PDF_DIR));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File generateCertificationPdf(Certification cert, User student) throws Exception {
        String fileName = "Certification_" + cert.getUniqueNumber() + ".pdf";
        File file = new File(PDF_DIR, fileName);

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36, Color.BLUE);
        Paragraph title = new Paragraph("CERTIFICAT DE RÉUSSITE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(50);
        document.add(title);

        document.add(new Paragraph("\n\n"));

        // Content
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 18);
        Paragraph content = new Paragraph(
                "Le présent certificat est décerné à :\n\n", contentFont);
        content.setAlignment(Element.ALIGN_CENTER);
        document.add(content);

        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        Paragraph name = new Paragraph(student.getFullName(), nameFont);
        name.setAlignment(Element.ALIGN_CENTER);
        document.add(name);

        Paragraph text = new Paragraph(
                "\nPour avoir validé avec succès la formation de :\n", contentFont);
        text.setAlignment(Element.ALIGN_CENTER);
        document.add(text);

        Font typeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
        Paragraph type = new Paragraph(cert.getCertificationType(), typeFont);
        type.setAlignment(Element.ALIGN_CENTER);
        document.add(type);

        document.add(new Paragraph("\n\n"));

        // Date and ID
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Paragraph footer = new Paragraph(
                "Délivré le : " + cert.getIssuedAt().format(fmt) + "\n" +
                "Numéro unique : " + cert.getUniqueNumber(),
                FontFactory.getFont(FontFactory.HELVETICA, 12));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        // QR Code
        String qrData = "https://edusmart.com/verify/" + cert.getUniqueNumber();
        com.lowagie.text.Image qrImage = generateQrCodeImage(qrData, 120, 120);
        qrImage.setAlignment(Element.ALIGN_RIGHT);
        qrImage.setAbsolutePosition(PageSize.A4.rotate().getWidth() - 150, 50);
        document.add(qrImage);

        document.close();
        return file;
    }

    public static File generateBulletinPdf(Bulletin bulletin, User student) throws Exception {
        String fileName = "Bulletin_" + student.getLastName() + "_" + bulletin.getAcademicYear() + ".pdf";
        File file = new File(PDF_DIR, fileName);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Paragraph header = new Paragraph("BULLETIN DE NOTES", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        document.add(new Paragraph("\n"));

        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        document.add(new Paragraph("Étudiant : " + student.getFullName(), infoFont));
        document.add(new Paragraph("Année Académique : " + bulletin.getAcademicYear(), infoFont));
        document.add(new Paragraph("Semestre : " + bulletin.getSemester(), infoFont));
        document.add(new Paragraph("\n"));

        // Summary Table
        Table table = new Table(2);
        table.setWidth(100);
        table.addCell("Moyenne Générale");
        table.addCell(bulletin.getAverage() != null ? String.valueOf(bulletin.getAverage()) : "N/A");
        table.addCell("Mention");
        table.addCell(bulletin.getMention() != null ? bulletin.getMention() : "N/A");
        table.addCell("Rang");
        table.addCell(bulletin.getClassRank() != null ? String.valueOf(bulletin.getClassRank()) : "N/A");
        table.addCell("Statut");
        table.addCell(bulletin.getStatus());
        document.add(table);

        document.add(new Paragraph("\n\n"));

        // QR Code
        String qrData = "https://edusmart.com/bulletin/" + bulletin.getId();
        com.lowagie.text.Image qrImage = generateQrCodeImage(qrData, 100, 100);
        qrImage.setAlignment(Element.ALIGN_LEFT);
        document.add(qrImage);

        document.close();
        return file;
    }

    private static com.lowagie.text.Image generateQrCodeImage(String data, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return com.lowagie.text.Image.getInstance(pngData);
    }
}
