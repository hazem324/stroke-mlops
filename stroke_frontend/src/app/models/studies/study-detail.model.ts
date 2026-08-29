import { Predictiond } from "../prediction/prediction-detail.model";

export interface StudyDetail {
  id: number;

  studyCode: string;
  studyDate: string;
  modality: string;
  status: string;

  patientId: number;
  patientCode: string;
  patientFullName: string;

  dwiFileName: string;
  dwiFileSize: number;

  prediction: Predictiond | null;

  errorMessage: string | null;

  createdAt: string;
  updatedAt: string;
}