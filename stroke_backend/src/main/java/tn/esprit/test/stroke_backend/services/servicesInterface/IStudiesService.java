package tn.esprit.test.stroke_backend.services.servicesInterface;

import java.util.List;

import tn.esprit.test.stroke_backend.dto.study.StudyRequest;
import tn.esprit.test.stroke_backend.entities.Studies;

public interface IStudiesService {

    Studies createStudy(Long patientId, StudyRequest request);

    List<Studies> getPatientStudies(Long patientId);

    Studies getStudy(Long studyId);


} 