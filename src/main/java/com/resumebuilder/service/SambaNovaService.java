package com.resumebuilder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.IOException;
import java.util.*;

@Service
public class SambaNovaService {

    @Value("${sambanova.api.key}")
    private String apiKey;

    @Value("${sambanova.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public Map<String, Object> getATSAnalysis(String resumeContent) {
        String prompt = "Act as a professional recruiter and ATS system. Rate the following resume content for structural integrity and readability by automated systems. " +
                "Provide a score from 0-100 and a brief analysis (max 3 sentences) of why this score was given. " +
                "Format your response as a JSON object with keys: 'score' (number) and 'analysis' (string). " +
                "Resume Content: " + resumeContent;

        return callSambaNova(prompt);
    }

    public Map<String, Object> getJobMatchAnalysis(String resumeContent, String jobDescription) {
        String prompt = "Act as a professional recruiter. Compare the following resume against the job description. " +
                "Provide a match score from 0-100, a list of 'matchedKeywords', and a list of 'missingKeywords'. " +
                "Format your response as a JSON object with keys: 'score' (number), 'matchedKeywords' (array of strings), and 'missingKeywords' (array of strings). " +
                "Resume: " + resumeContent + "\n\nJob Description: " + jobDescription;

        return callSambaNova(prompt);
    }

    private Map<String, Object> callSambaNova(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "Meta-Llama-3.1-8B-Instruct");
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("response_format", Collections.singletonMap("type", "json_object"));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
                    // Parse the JSON content from the LLM response
                    return new com.fasterxml.jackson.databind.ObjectMapper().readValue(content, Map.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Fallback in case of error
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("score", 0);
        fallback.put("error", "Failed to connect to AI engine.");
        return fallback;
    }
}
