export interface RecentAnalysis {
  studyId: number;
  patientId: number;
  patientCode: string;
  patientName: string;
  studyCode: string;
  studyDate: string;
  modality: string | null;
  status: string | null;
  lesionDetected: boolean | null;
  createdAt: string;
}