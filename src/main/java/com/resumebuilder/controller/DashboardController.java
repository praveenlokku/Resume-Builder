package com.resumebuilder.controller;

import com.resumebuilder.model.Resume;
import com.resumebuilder.model.User;
import com.resumebuilder.service.AISuggestionService;
import com.resumebuilder.service.PDFGenerationService;
import com.resumebuilder.service.ResumeService;
import com.resumebuilder.service.SambaNovaService;
import com.resumebuilder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.lowagie.text.DocumentException;
import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            List<Resume> resumes = resumeService.getResumesByUser(userOpt.get());
            resumes.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
            model.addAttribute("resumes", resumes);
        }
        return "dashboard";
    }

    @GetMapping("/archive")
    public String archiveHistory(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            List<Resume> archivedResumes = resumeService.getArchivedResumesByUser(userOpt.get());
            archivedResumes.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
            model.addAttribute("archivedResumes", archivedResumes);
        }
        return "archive";
    }

    @GetMapping("/new")
    public String selectTemplate() {
        return "template-select";
    }

    @GetMapping("/create")
    public String createResumeForm(@RequestParam(required = false) Integer templateId, Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        
        Resume resume = new Resume();
        resume.setTitle("");
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Auto-fill from settings
            int tid = (templateId != null) ? templateId : user.getDefaultTemplateId();
            resume.setTemplateId(tid);
            
            // Build initial content JSON with professional markers
            String initialContent = String.format(
                "{\"fullName\":\"%s\",\"location\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"socialLinks\":[%s,%s,%s]}",
                user.getFullName() != null ? user.getFullName() : "",
                user.getLocation() != null ? user.getLocation() : "",
                user.getPhone() != null ? user.getPhone() : "",
                user.getUsername(),
                user.getLinkedin() != null && !user.getLinkedin().isEmpty() ? "{\"platform\":\"LinkedIn\",\"url\":\"" + user.getLinkedin() + "\"}" : "null",
                user.getGithub() != null && !user.getGithub().isEmpty() ? "{\"platform\":\"GitHub\",\"url\":\"" + user.getGithub() + "\"}" : "null",
                user.getPortfolio() != null && !user.getPortfolio().isEmpty() ? "{\"platform\":\"Portfolio\",\"url\":\"" + user.getPortfolio() + "\"}" : "null"
            ).replace(",null", "").replace("null,", "").replace("[null]", "[]");
            
            resume.setContent(initialContent);
        } else {
            resume.setTemplateId(templateId != null ? templateId : 1);
            resume.setContent("{}");
        }
        
        model.addAttribute("resume", resume);
        return "resume-form";
    }

    @GetMapping("/edit/{id}")
    public String editResumeForm(@PathVariable Long id, Model model) {
        Optional<Resume> resumeOpt = resumeService.getResumeById(id);
        if (resumeOpt.isPresent()) {
            model.addAttribute("resume", resumeOpt.get());
            return "resume-form";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/save")
    public String saveResume(@ModelAttribute Resume resume, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            resume.setUser(userOpt.get());
            Resume savedResume = resumeService.saveResume(resume);
            redirectAttributes.addFlashAttribute("success", "Profile successfully synchronized.");
            return "redirect:/dashboard?download=" + savedResume.getId();
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/api/save")
    @ResponseBody
    public java.util.Map<String, Object> autoSave(@RequestBody java.util.Map<String, Object> payload, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (userOpt.isPresent()) {
            try {
                Object idObj = payload.get("id");
                Long id = (idObj != null && !idObj.toString().trim().isEmpty()) ? Long.parseLong(idObj.toString()) : null;
                String title = (String) payload.get("title");
                String content = (String) payload.get("content");
                int templateId = Integer.parseInt(payload.get("templateId").toString());

                Resume resume = new Resume();
                if (id != null) resume.setId(id);
                resume.setTitle(title);
                resume.setContent(content);
                resume.setTemplateId(templateId);
                resume.setUser(userOpt.get());

                resumeService.saveResume(resume);
                
                response.put("status", "success");
                response.put("lastSaved", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            } catch (Exception e) {
                e.printStackTrace(); // Log it!
                response.put("status", "error");
                response.put("message", e.getMessage());
            }
        } else {
            response.put("status", "error");
            response.put("message", "User not authenticated");
        }
        return response;
    }

    @Autowired
    private PDFGenerationService pdfGenerationService;

    @Autowired
    private AISuggestionService aiSuggestionService;

    @Autowired
    private SambaNovaService sambaNovaService;

    @PostMapping("/api/ai/ats-check")
    @ResponseBody
    public java.util.Map<String, Object> atsCheck(
            @RequestParam(value="content", required = false) String content,
            @RequestParam(value="file", required = false) MultipartFile file) {
        
        System.out.println("ATS Check Request received. File present: " + (file != null && !file.isEmpty()));
        String textToAnalyze = content;
        
        try {
            if (file != null && !file.isEmpty()) {
                textToAnalyze = sambaNovaService.extractTextFromPdf(file);
                System.out.println("Extracted text sample: " + (textToAnalyze.length() > 50 ? textToAnalyze.substring(0, 50) : textToAnalyze));
            }
            
            if (textToAnalyze == null || textToAnalyze.trim().isEmpty()) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("error", "No content provided. Please upload a PDF or paste resume text.");
                return err;
            }
            
            return sambaNovaService.getATSAnalysis(textToAnalyze);
        } catch (Exception e) {
            e.printStackTrace();
            java.util.Map<String, Object> err = new java.util.HashMap<>();
            err.put("error", "System Error: " + e.getMessage());
            return err;
        }
    }

    @PostMapping("/api/ai/job-match")
    @ResponseBody
    public java.util.Map<String, Object> jobMatch(
            @RequestParam(value="resumeContent", required = false) String resumeContent,
            @RequestParam(value="jobDescription", required = false) String jobDescription,
            @RequestParam(value="file", required = false) MultipartFile file) {
        
        System.out.println("Job Match Request received. File present: " + (file != null && !file.isEmpty()));
        String textToAnalyze = resumeContent;
        
        try {
            if (file != null && !file.isEmpty()) {
                textToAnalyze = sambaNovaService.extractTextFromPdf(file);
            }

            if (textToAnalyze == null || textToAnalyze.trim().isEmpty()) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("error", "No resume content provided. Please upload a PDF or select a saved resume.");
                return err;
            }
            
            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("error", "Please provide a Target Job Description.");
                return err;
            }
            
            return sambaNovaService.getJobMatchAnalysis(textToAnalyze, jobDescription);
        } catch (Exception e) {
            e.printStackTrace();
            java.util.Map<String, Object> err = new java.util.HashMap<>();
            err.put("error", "System Error: " + e.getMessage());
            return err;
        }
    }

    @GetMapping("/download/{id}")
    public void downloadResume(@PathVariable Long id, HttpServletResponse response) throws IOException, DocumentException {
        Optional<Resume> resumeOpt = resumeService.getResumeById(id);
        if (resumeOpt.isPresent()) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=resume.pdf");
            pdfGenerationService.generateResume(resumeOpt.get(), response.getOutputStream());
        }
    }

    @GetMapping("/suggestions/{id}")
    public String viewSuggestions(@PathVariable Long id, Model model) {
        Optional<Resume> resumeOpt = resumeService.getResumeById(id);
        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            model.addAttribute("resume", resume);
            model.addAttribute("suggestions", aiSuggestionService.getSuggestions(resume.getContent()));
        }
        return "suggestions";
    }

    @PostMapping("/archive/{id}")
    public String archiveResume(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        resumeService.archiveResume(id);
        redirectAttributes.addFlashAttribute("success", "Profile archived successfully. You can find it in the Archive History.");
        return "redirect:/dashboard";
    }

    @PostMapping("/unarchive/{id}")
    public String unarchiveResume(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        resumeService.unarchiveResume(id);
        redirectAttributes.addFlashAttribute("success", "Profile restored successfully.");
        return "redirect:/dashboard/archive";
    }

    @PostMapping("/delete/{id}")
    public String deleteResume(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        resumeRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Profile permanently deleted.");
        return "redirect:/dashboard/archive";
    }
}
