package tn.esprit.test.stroke_backend.services.servicesInterface;

import tn.esprit.test.stroke_backend.entities.GeneratedSections;
import tn.esprit.test.stroke_backend.entities.Prediction;

public interface IGeminiReportService {
    
    GeneratedSections generateReport(Prediction prediction);
}
