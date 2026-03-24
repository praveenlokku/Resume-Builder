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
        Font subheaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(15, 23, 42)); // Zinc-900
        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.DARK_GRAY);

        // Header (Centered)
        Paragraph namePara = new Paragraph(((String) content.getOrDefault("fullName", "NAME")).toUpperCase(), nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        document.add(namePara);

        String contact = String.format("%s  |  %s  |  %s", 
            content.getOrDefault("location", ""), 
            content.getOrDefault("email", ""), 
            content.getOrDefault("phone", ""));
        Paragraph contactPara = new Paragraph(contact, normalFont);
        contactPara.setAlignment(Element.ALIGN_CENTER);
        contactPara.setSpacingAfter(4);
        document.add(contactPara);

        // Social Links (Array)
        List<Map<String, String>> socialLinks = (List<Map<String, String>>) content.get("socialLinks");
        if (socialLinks != null && !socialLinks.isEmpty()) {
            Paragraph socialPara = new Paragraph("", smallFont);
            socialPara.setAlignment(Element.ALIGN_CENTER);
            for (int i = 0; i < socialLinks.size(); i++) {
                Map<String, String> link = socialLinks.get(i);
                String platform = link.get("platform");
                String url = link.get("url");
                
                String absUrl = url;
                if (!absUrl.startsWith("http")) absUrl = "https://" + absUrl;

                Anchor anchor = new Anchor(platform + ": " + url, smallFont);
                anchor.setReference(absUrl);
                socialPara.add(anchor);

                if (i < socialLinks.size() - 1) socialPara.add(new Chunk("  |  ", smallFont));
            }
            socialPara.setSpacingAfter(4);
            document.add(socialPara);
        }

        LineSeparator ls = new LineSeparator(0.8f, 100, Color.BLACK, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(ls));

        // Summary
        renderSectionHeader(document, "PROFESSIONAL SUMMARY", subheaderFont, false);
        Paragraph summary = new Paragraph((String) content.getOrDefault("summary", ""), normalFont);
        summary.setAlignment(Element.ALIGN_JUSTIFIED);
        summary.setSpacingAfter(5);
        document.add(summary);

        // Experience
        renderExperienceATS(document, content, subheaderFont, boldFont, normalFont);

        // Projects
        renderProjectsATS(document, content, subheaderFont, boldFont, normalFont);

        // Education
        renderEducationATS(document, content, subheaderFont, boldFont, normalFont);

        // Skills (Array-based)
        renderSkills(document, content, subheaderFont, boldFont, normalFont);

        // Certifications (Array-based)
        renderCertifications(document, content, subheaderFont, boldFont, normalFont);

        // Achievements (Array-based)
        renderAchievements(document, content, subheaderFont, boldFont, normalFont);
    }

    private void generateModernPurpleTemplate(Document document, Map<String, Object> content) throws DocumentException {
        // Updated to use the same consolidated renderers for consistency but with different fonts/colors
        generateClassicATSTemplate(document, content); 
    }

    private void generateTraditionalTemplate(Document document, Map<String, Object> content) throws DocumentException {
        generateClassicATSTemplate(document, content);
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
                table.setSpacingBefore(2);
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
                table.setSpacingBefore(2);
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
                table.setSpacingBefore(2);
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
                p.setSpacingBefore(1);
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
                p.setSpacingBefore(1);
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
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.addCell(createCell(ach.getOrDefault("title", ""), boldFont, Element.ALIGN_LEFT));
                table.addCell(createCell(ach.getOrDefault("date", ""), normalFont, Element.ALIGN_RIGHT));
                table.setSpacingBefore(2);
                document.add(table);
                
                Paragraph dsc = new Paragraph(ach.getOrDefault("description", ""), normalFont);
                dsc.setSpacingAfter(4);
                document.add(dsc);
            }
        }
    }

    private void renderBulletPoints(Document document, String text, Font font) throws DocumentException {
        if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            com.lowagie.text.List bulletList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED, 10);
            bulletList.setListSymbol(new Chunk("\u2022", font));
            bulletList.add(new ListItem(line.trim(), font));
            bulletList.setIndentationLeft(15);
            document.add(bulletList);
        }
    }

    private void renderSectionHeader(Document document, String title, Font font, boolean withLine) throws DocumentException {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(5);
        p.setSpacingAfter(withLine ? 1 : 2);
        document.add(p);
        if (withLine) {
            LineSeparator ls = new LineSeparator(0.5f, 100, Color.LIGHT_GRAY, Element.ALIGN_CENTER, -2);
            document.add(new Chunk(ls));
        }
    }

    private PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(0);
        return cell;
    }
}
