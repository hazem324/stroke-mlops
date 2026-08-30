import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { StudiesResponse, Study } from '../models/studies/studies.model';
import { StudyDetail } from '../models/studies/study-detail.model';
import { RecentAnalysis } from '../models/studies/RecentAnalysis.model';
export type Modality = 'DWI';

@Injectable({
  providedIn: 'root'
})
export class StudiesService {

  

  private readonly baseUri = environment.apiBaseUrl + '/api/studies';


  constructor(private http: HttpClient) { }


  analyzeStudy (patientId: number, file: File, modality: Modality = 'DWI') : Observable<Study> {

    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Study>(
      `${this.baseUri}/${patientId}/analyze?modality=${modality}`,
      formData
    );
  }

  getStudiesByPatient(patientId: number): Observable<StudiesResponse> {

  return this.http.get<StudiesResponse>(
    `${this.baseUri}/patients/${patientId}`
  );
}

 getStudy(studyId: number): Observable<Study> {

    return this.http.get<Study>(
      `${this.baseUri}/${studyId}`
    );
  }

  getDetaileAnalyse (studyId: number): Observable<StudyDetail> {
    return this.http.get<StudyDetail> (
      `${this.baseUri}/study-detail/${studyId}`
    );
  }

  getAnalysesHistory(): Observable<RecentAnalysis[]> {
    return this.http.get<RecentAnalysis[]>(
       `${this.baseUri}/analysis-history`
    )
  }
}