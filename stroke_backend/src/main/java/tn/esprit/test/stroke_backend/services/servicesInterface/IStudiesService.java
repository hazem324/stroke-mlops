package tn.esprit.test.stroke_backend.services.servicesInterface;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import tn.esprit.test.stroke_backend.dto.study.StudyRequest;
import tn.esprit.test.stroke_backend.dto.study.StudyResponseDTO;
import tn.esprit.test.stroke_backend.entities.Modality;
import tn.esprit.test.stroke_backend.entities.Studies;

public interface IStudiesService {

    Studies createStudy(Long patientId, StudyRequest request);

    List<Studies> getPatientStudies(Long patientId);

    Studies getStudy(Long studyId);

    StudyResponseDTO analyzeStudy( Long patientId, MultipartFile file, Modality modality);

} 