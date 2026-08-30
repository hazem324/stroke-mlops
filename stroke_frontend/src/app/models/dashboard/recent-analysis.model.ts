export interface RecentAnalysis {
  studyId: number;
  patientId: number;
  patientCode: string;
  patientName: string;
  studyCode: string;
  studyDate: string;
  modality: string;
  status: string;
  lesionDetected: boolean | null;
  createdAt: string;
}