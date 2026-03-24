package com.resumebuilder.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AISuggestionService {

    public List<String> getSuggestions(String resumeContent) {
        List<String> suggestions = new ArrayList<>();
        
        // Mock logic for suggestions
        if (resumeContent == null || resumeContent.isEmpty()) {
            suggestions.add("Add a professional summary to highlight your key skills.");
            return suggestions;
        }

        if (!resumeContent.contains("experience")) {
            suggestions.add("Include more details about your work experience to show your career growth.");
        }
        
        if (!resumeContent.contains("skills")) {
            suggestions.add("Add a dedicated skills section with relevant keywords (e.g., Java, Python, Project Management).");
        }

        suggestions.add("Use action verbs like 'Led', 'Developed', and 'Managed' to describe your achievements.");
        suggestions.add("Quantify your results (e.g., 'Increased efficiency by 20%') where possible.");
        suggestions.add("Ensure your contact information is up to date and professional.");
        
        return suggestions;
    }
}
