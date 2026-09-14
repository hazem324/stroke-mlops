package tn.esprit.test.stroke_backend.services;

import org.springframework.beans.factory.annotation.Value;

import tn.esprit.test.stroke_backend.services.servicesInterface.IGeminiReportService;

public class GeminiReportService  implements  IGeminiReportService{

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value ("${gemini.model}")
    private String geminiModel;
    @Value ("${gemini.base.url}")
    private String geminiBaseUrl;
    
    

    
}
