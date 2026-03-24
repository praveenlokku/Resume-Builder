package com.resumebuilder.controller;

import com.resumebuilder.model.Resume;
import com.resumebuilder.model.User;
import com.resumebuilder.service.AISuggestionService;
import com.resumebuilder.service.PDFGenerationService;
import com.resumebuilder.service.ResumeService;
import com.resumebuilder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/new")
    public String selectTemplate() {
        return "template-select";
    }

    @GetMapping("/create")
    public String createResumeForm(@RequestParam(defaultValue = "1") int templateId, Model model) {
        Resume resume = new Resume();
        resume.setTitle("");
        resume.setContent("{}");
        resume.setTemplateId(templateId);
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

    @Autowired
    private PDFGenerationService pdfGenerationService;

    @Autowired
    private AISuggestionService aiSuggestionService;

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

    @GetMapping("/delete/{id}")
    public String deleteResume(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        resumeService.deleteResume(id);
        redirectAttributes.addFlashAttribute("success", "Profile archived successfully.");
        return "redirect:/dashboard";
    }
}
