import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

export interface GenerateReportResponse {
  message: string;
  reportPath: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private readonly baseUri = environment.apiBaseUrl + '/api/medical-reports';

  constructor(private http: HttpClient) {}

  generateReport(predictionId: number): Observable<GenerateReportResponse> {
    return this.http.post<GenerateReportResponse>(
      `${this.baseUri}/generate/${predictionId}`,
      {}
    );
  }

  
  downloadReport(studyId: number): Observable<Blob> {
    return this.http.get(
      `${this.baseUri}/${studyId}/medical-report/download`,
      { responseType: 'blob' }
    );
  }
}