export type Sex = 'MALE' | 'FEMALE';

export interface Patient {
  id: number;
  patientCode: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  sex: Sex;
  age: number;
  weight: number;
  phoneNumber: string;
}

export interface CreatePatientRequest {
  patientCode: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  sex: Sex;
  age: number;
  weight: number;
  phoneNumber: string;
}

export interface UpdatePatientRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  sex: Sex;
  age: number;
  weight: number;
  phoneNumber: string;
}

export interface CreatePatientResponse {
  message: string;
  patientId: number;
  patientCode: string;
}

export interface PatientResponse {
  patient: Patient;
}

export interface PatientsResponse {
  patients: Patient[];
}