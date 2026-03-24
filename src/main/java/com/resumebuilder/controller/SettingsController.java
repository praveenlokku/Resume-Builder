package com.resumebuilder.controller;

import com.resumebuilder.model.User;
import com.resumebuilder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/dashboard/settings")
public class SettingsController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String settings(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
        }
        return "settings";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String fullName, @RequestParam String location, 
                                Authentication authentication, RedirectAttributes redirectAttributes) {
        userService.updateProfile(authentication.getName(), fullName, location);
        redirectAttributes.addFlashAttribute("success", "General profile successfully synchronized.");
        return "redirect:/dashboard/settings";
    }

    @PostMapping("/professional")
    public String updateProfessional(@RequestParam String phone, @RequestParam String linkedin,
                                   @RequestParam String github, @RequestParam String portfolio,
                                   Authentication authentication, RedirectAttributes redirectAttributes) {
        userService.updateProfessionalLinks(authentication.getName(), phone, linkedin, github, portfolio);
        redirectAttributes.addFlashAttribute("success", "Professional markers updated successfully!");
        return "redirect:/dashboard/settings";
    }

    @PostMapping("/preferences")
    public String updatePreferences(@RequestParam int defaultTemplateId, 
                                   Authentication authentication, RedirectAttributes redirectAttributes) {
        userService.updatePreferences(authentication.getName(), defaultTemplateId);
        redirectAttributes.addFlashAttribute("success", "System preferences saved.");
        return "redirect:/dashboard/settings";
    }

    @PostMapping("/security")
    public String updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword, 
                                 Authentication authentication, RedirectAttributes redirectAttributes) {
        boolean success = userService.updatePassword(authentication.getName(), oldPassword, newPassword);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Credential update confirmed.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Security validation failed: Incorrect current password.");
        }
        return "redirect:/dashboard/settings";
    }

    @Autowired
    private com.resumebuilder.service.ResumeService resumeService;
    
    @GetMapping("/export")
    public void exportData(Authentication authentication, javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            java.util.List<com.resumebuilder.model.Resume> resumes = resumeService.getResumesByUser(user);
            
            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment; filename=resumepro_export.json");
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), resumes);
        }
    }

    @PostMapping("/delete")
    public String deleteAccount(Authentication authentication, javax.servlet.http.HttpSession session) {
        userService.deleteUser(authentication.getName());
        session.invalidate();
        return "redirect:/login?deleted";
    }
}
