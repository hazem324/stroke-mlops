import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';

import {Patient, CreatePatientRequest, UpdatePatientRequest, CreatePatientResponse, PatientResponse, PatientsResponse} from '../models/patient/patient.model';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  private readonly baseUrl =
    environment.apiBaseUrl + '/api/patient';

  constructor(private http: HttpClient) {}

  /**
   * GET /api/patient/by-docktor
   * Get all patients belonging to the authenticated doctor.
   */
  getMyPatients() {
    return this.http.get<PatientsResponse>(
      `${this.baseUrl}/by-docktor`
    );
  }

  /**
   * GET /api/patient/{id}
   * Get one patient.
   */
  getPatientById(id: number) {
    return this.http.get<PatientResponse>(
      `${this.baseUrl}/${id}`
    );
  }

  /**
   * POST /api/patient
   * Create a new patient.
   */
  createPatient(patient: CreatePatientRequest) {
    return this.http.post<CreatePatientResponse>(
      this.baseUrl,
      patient
    );
  }

  /**
   * PUT /api/patient/{id}
   * Update an existing patient.
   */
  updatePatient(
    id: number,
    patient: UpdatePatientRequest
  ) {
    return this.http.put<CreatePatientResponse>(
      `${this.baseUrl}/${id}`,
      patient
    );
  }
}