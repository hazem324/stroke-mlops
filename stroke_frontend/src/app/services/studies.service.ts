import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Study } from '../models/studies/studies.model';
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


}
