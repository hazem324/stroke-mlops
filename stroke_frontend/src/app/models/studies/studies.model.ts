import { Prediction } from "../prediction/prediction.model";

export interface Study {
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

  prediction?: Prediction;

  errorMessage?: string | null;

  createdAt: string;
  updatedAt: string;
}