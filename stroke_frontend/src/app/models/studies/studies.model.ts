import { Prediction } from '../prediction/prediction.model';

export interface StudyPatient {
  id: number;
  patientCode: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  sex: string;
  age: number;
  weight: number;
  phoneNumber: string | null;
}

export interface Study {
  id: number;
  studyCode: string;
  studyDate: string;
  modality: string;
  status: string;

  patient: StudyPatient;

  dwiFileName: string;
  dwiStoragePath: string;
  dwiFileSize: number;

  prediction?: Prediction;

  errorMessage: string | null;

  createdAt: string;
  updatedAt: string;
}

export interface StudiesResponse {
  studies: Study[];
}