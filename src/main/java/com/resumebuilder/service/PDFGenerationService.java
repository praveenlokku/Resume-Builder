package com.resumebuilder.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.resumebuilder.model.Resume;
import org.springframework.stereotype.Service;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.awt.Color;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import java.util.List;

import com.lowagie.text.pdf.draw.LineSeparator;

@Service
public class PDFGenerationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public void generateResume(Resume resume, OutputStream out) throws DocumentException {
        // Enforce A4 Portrait with fixed margins
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, out);
        document.open();

        try {
            Map<String, Object> content = objectMapper.readValue(resume.getContent(), Map.class);
            int templateId = resume.getTemplateId();

            // All templates now honor the same data-binding and One-Page logic
            if (templateId == 2) {
                generateModernPurpleTemplate(document, content);
            } else if (templateId == 3) {
                generateTraditionalTemplate(document, content);
            } else {
                generateClassicATSTemplate(document, content);
            }

        } catch (Exception e) {
            document.add(new Paragraph("Error generating premium PDF: " + e.getMessage()));
        } finally {
            document.close();
        }
    }

    private void generateClassicATSTemplate(Document document, Map<String, Object> content) throws DocumentException {
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
        Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(15, 23, 42)); // Zinc-900
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
        
        renderUnifiedTemplate(document, content, nameFont, hFont, boldFont, normalFont, Color.BLACK);
    }

    private void renderSocialLinks(Document document, Map<String, Object> content, Font font) throws DocumentException {
        List<Map<String, String>> socialLinks = (List<Map<String, String>>) content.get("socialLinks");
        if (socialLinks != null && !socialLinks.isEmpty()) {
            Paragraph socialPara = new Paragraph("", font);
            socialPara.setAlignment(Element.ALIGN_CENTER);
            for (int i = 0; i < socialLinks.size(); i++) {
                Map<String, String> link = socialLinks.get(i);
                String platform = link.get("platform");
                String url = link.get("url");
                String absUrl = url.startsWith("http") ? url : "https://" + url;

                Anchor anchor = new Anchor(platform + ": " + url, font);
                anchor.setReference(absUrl);
                socialPara.add(anchor);
                if (i < socialLinks.size() - 1) socialPara.add(new Chunk("  |  ", font));
            }
            socialPara.setSpacingAfter(4);
            document.add(socialPara);
        }
    }

    private void generateModernPurpleTemplate(Document document, Map<String, Object> content) throws DocumentException {
        // Modern Sans-Serif with Indigo Accents
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(79, 70, 229)); // Indigo-600
        Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(79, 70, 229));
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
        
        renderUnifiedTemplate(document, content, nameFont, hFont, boldFont, normalFont, new Color(79, 70, 229));
    }

    private void generateTraditionalTemplate(Document document, Map<String, Object> content) throws DocumentException {
        // Traditional Serif (Academic/Legal)
        Font nameFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 22, Color.BLACK);
        Font hFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 11, Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 9, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.TIMES, 9, Color.BLACK);
        
        renderUnifiedTemplate(document, content, nameFont, hFont, boldFont, normalFont, Color.BLACK);
    }

    private void renderUnifiedTemplate(Document document, Map<String, Object> content, Font nameFont, Font hFont, Font boldFont, Font normalFont, Color accent) throws DocumentException {
        // Shared professional 1-page logic
        Paragraph namePara = new Paragraph(((String) content.getOrDefault("fullName", "NAME")).toUpperCase(), nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        document.add(namePara);

        String contact = String.format("%s  |  %s  |  %s", content.getOrDefault("location", ""), content.getOrDefault("email", ""), content.getOrDefault("phone", ""));
        Paragraph cp = new Paragraph(contact, normalFont);
        cp.setAlignment(Element.ALIGN_CENTER);
        cp.setSpacingAfter(4);
        document.add(cp);

        renderSocialLinks(document, content, normalFont);
        
        LineSeparator ls = new LineSeparator(0.8f, 100, accent, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(ls));

        renderSectionHeader(document, "PROFESSIONAL SUMMARY", hFont, false);
        Paragraph summary = new Paragraph((String) content.getOrDefault("summary", ""), normalFont);
        summary.setAlignment(Element.ALIGN_JUSTIFIED);
        summary.setSpacingAfter(5);
        document.add(summary);

        renderExperienceATS(document, content, hFont, boldFont, normalFont);
        renderProjectsATS(document, content, hFont, boldFont, normalFont);
        renderEducationATS(document, content, hFont, boldFont, normalFont);
        renderSkills(document, content, hFont, boldFont, normalFont);
        renderCertifications(document, content, hFont, boldFont, normalFont);
        renderAchievements(document, content, hFont, boldFont, normalFont);
    }

    @SuppressWarnings("unchecked")
    private void renderExperienceATS(Document document, Map<String, Object> content, Font titleFont, Font boldFont, Font normalFont) throws DocumentException {
        List<Map<String, String>> experience = (List<Map<String, String>>) content.get("experience");
        if (experience != null && !experience.isEmpty()) {
            renderSectionHeader(document, "WORK EXPERIENCE", titleFont, true);
            for (Map<String, String> exp : experience) {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.addCell(createCell(exp.getOrDefault("company", ""), boldFont, Element.ALIGN_LEFT));
                table.addCell(createCell(exp.getOrDefault("duration", ""), normalFont, Element.ALIGN_RIGHT));
                table.setSpacingBefore(0); // Set to 0 to hug the section line
                document.add(table);

                document.add(new Paragraph(exp.getOrDefault("title", ""), boldFont));
                renderBulletPoints(document, exp.getOrDefault("description", ""), normalFont);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void renderProjectsATS(Document document, Map<String, Object> content, Font titleFont, Font boldFont, Font normalFont) throws DocumentException {
        List<Map<String, String>> projects = (List<Map<String, String>>) content.get("projects");
        if (projects != null && !projects.isEmpty()) {
            renderSectionHeader(document, "KEY PROJECTS", titleFont, true);
            for (Map<String, String> proj : projects) {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.addCell(createCell(proj.getOrDefault("title", ""), boldFont, Element.ALIGN_LEFT));
                table.addCell(createCell(proj.getOrDefault("date", ""), normalFont, Element.ALIGN_RIGHT));
                table.setSpacingBefore(0); // Set to 0 to hug the section line
                document.add(table);

                renderBulletPoints(document, proj.getOrDefault("description", ""), normalFont);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void renderEducationATS(Document document, Map<String, Object> content, Font titleFont, Font boldFont, Font normalFont) throws DocumentException {
        List<Map<String, String>> education = (List<Map<String, String>>) content.get("education");
        if (education != null && !education.isEmpty()) {
            renderSectionHeader(document, "EDUCATION", titleFont, true);
            for (Map<String, String> edu : education) {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.addCell(createCell(edu.getOrDefault("school", ""), boldFont, Element.ALIGN_LEFT));
                table.addCell(createCell(edu.getOrDefault("year", ""), normalFont, Element.ALIGN_RIGHT));
                table.setSpacingBefore(0); // Set to 0 to hug the section line
                table.setSpacingAfter(2); // Reduced from 5 for tighter flow
                document.add(table);
                document.add(new Paragraph(edu.getOrDefault("degree", ""), normalFont));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void renderSkills(Document document, Map<String, Object> content, Font titleFont, Font boldFont, Font normalFont) throws DocumentException {
        List<Map<String, String>> skillGroups = (List<Map<String, String>>) content.get("skillGroups");
        if (skillGroups != null && !skillGroups.isEmpty()) {
            renderSectionHeader(document, "CORE SKILLS", titleFont, true);
            for (Map<String, String> group : skillGroups) {
                Paragraph p = new Paragraph();
                p.add(new Chunk(group.getOrDefault("category", "") + ": ", boldFont));
                p.add(new Chunk(group.getOrDefault("values", ""), normalFont));
                p.setSpacingBefore(0);
                p.setSpacingAfter(2); // Goldilocks fit
                p.setLeading(normalFont.getSize() * 1.32f); // Goldilocks leading
                document.add(p);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void renderCertifications(Document document, Map<String, Object> content, Font titleFont, Font boldFont, Font normalFont) throws DocumentException {
        List<Map<String, String>> certifications = (List<Map<String, String>>) content.get("certifications");
        if (certifications != null && !certifications.isEmpty()) {
            renderSectionHeader(document, "CERTIFICATIONS", titleFont, true);
            for (Map<String, String> cert : certifications) {
                Paragraph p = new Paragraph();
                p.add(new Chunk(cert.getOrDefault("category", "") + ": ", boldFont));
                p.add(new Chunk(cert.getOrDefault("name", ""), normalFont));
                p.setSpacingBefore(0);
                p.setSpacingAfter(0);
                p.setLeading(normalFont.getSize() * 1.3f); // Final refined leading
                document.add(p);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void renderAchievements(Document document, Map<String, Object> content, Font titleFont, Font boldFont, Font normalFont) throws DocumentException {
        List<Map<String, String>> achievements = (List<Map<String, String>>) content.get("achievements");
        if (achievements != null && !achievements.isEmpty()) {
            renderSectionHeader(document, "ACHIEVEMENTS", titleFont, true);
            for (Map<String, String> ach : achievements) {
                Paragraph p = new Paragraph();
                String title = ach.getOrDefault("title", "");
                String date = ach.getOrDefault("date", "");
                String desc = ach.getOrDefault("description", "");
                
                if (!title.isEmpty()) {
                    p.add(new Chunk(title + (date.isEmpty() ? "" : " (" + date + ")") + ": ", boldFont));
                }
                p.add(new Chunk(desc, normalFont));
                p.setSpacingBefore(0);
                p.setSpacingAfter(0);
                p.setLeading(normalFont.getSize() * 1.3f); // Final refined leading
                document.add(p);
            }
        }
    }

    private void renderBulletPoints(Document document, String text, Font font) throws DocumentException {
        if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n");
        com.lowagie.text.List bulletList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED, 10);
        bulletList.setListSymbol(new Chunk("\u2022", font));
        bulletList.setIndentationLeft(12); // Slightly tighter
        boolean hasItems = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            bulletList.add(new ListItem(trimmed, font));
            hasItems = true;
        }
        if (hasItems) {
            document.add(bulletList);
        }
    }

    private void renderSectionHeader(Document document, String title, Font font, boolean withLine) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5); // Goldilocks spacing (5 before, 3 after)
        table.setSpacingAfter(3);
        
        PdfPCell cell = new PdfPCell(new Phrase(title.toUpperCase(), font));
        cell.setBorder(PdfPCell.NO_BORDER);
        if (withLine) {
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderColorBottom(Color.LIGHT_GRAY);
        }
        cell.setPaddingBottom(2.2f); // Goldilocks line gap
        cell.setPaddingLeft(0);
        cell.setPaddingTop(0);
        table.addCell(cell);
        document.add(table);
    }

    private PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(0);
        return cell;
    }
}
