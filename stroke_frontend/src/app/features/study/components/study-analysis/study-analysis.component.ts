import {Component, ElementRef, ViewChild, OnInit} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { PatientService } from '../../../../services/patient.service';
import { Patient } from '../../../../models/patient/patient.model';
import { ToastService } from '../../../../services/toast.service';
import { StudiesService } from '../../../../services/studies.service';


@Component({
  selector: 'app-study-analysis',
  templateUrl: './study-analysis.component.html',
  styleUrls: ['./study-analysis.component.css']
})
export class StudyAnalysisComponent implements OnInit {

  constructor(private patientService: PatientService, private toastService:ToastService, private studiesService:StudiesService){}

  ngOnInit(): void {
  this.patientService.getMyPatients().subscribe({
    next: (response) => {
      this.patients = response.patients;
    },
    error: (error: HttpErrorResponse) => {

  console.error('Error loading patients:', error);

  if (error.status === 403) {
    this.toastService.error(
      'Vous n’avez pas l’autorisation d’accéder aux patients.',
      'Accès refusé'
    );
    return;
  }

  if (error.status === 404) {
    this.toastService.error(
      'Aucun patient n’a été trouvé.',
      'Patients introuvables'
    );
    return;
  }

  if (error.status === 500) {
    this.toastService.error(
      'Une erreur interne est survenue sur le serveur.',
      'Erreur serveur'
    );
    return;
  }

  this.toastService.error(
    'Impossible de récupérer les patients.',
    'Erreur'
  );
}
  });
}

  @ViewChild('fileInput')
  fileInput!: ElementRef<HTMLInputElement>;

  patients: Patient[] = [];

  selectedPatientId: number | null = null;
  selectedPatient: Patient | null = null;
  selectedFile: File | null = null;
  isDragging = false;

  isLoading = false;


  onPatientChange(): void {

    if (this.selectedPatientId === null) {
      this.selectedPatient = null;
      return;
    }

    this.selectedPatient =
      this.patients.find(
        patient => patient.id === this.selectedPatientId
      ) ?? null;
  }


  openFileSelector(): void {

    if (this.isLoading) {
      return;
    }

    this.fileInput.nativeElement.click();
  }


  onFileSelected(event: Event): void {

    const input =
      event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];

    this.handleFile(file);
  }


  onDragEnter(event: DragEvent): void {

    event.preventDefault();

    if (this.isLoading) {
      return;
    }

    this.isDragging = true;
  }


  onDragOver(event: DragEvent): void {

    event.preventDefault();

    if (this.isLoading) {
      return;
    }

    this.isDragging = true;
  }


  onDragLeave(event: DragEvent): void {

    event.preventDefault();

    this.isDragging = false;
  }


  onDrop(event: DragEvent): void {

    event.preventDefault();

    this.isDragging = false;

    if (this.isLoading) {
      return;
    }

    if (!event.dataTransfer) {
      return;
    }

    const files = event.dataTransfer.files;

    if (!files || files.length === 0) {
      return;
    }

    const file = files[0];

    this.handleFile(file);
  }


  private handleFile(file: File): void {

    if (!this.isValidFile(file)) {
      return;
    }

    this.selectedFile = file;
  }


  private isValidFile(file: File): boolean {

    const fileName =
      file.name.toLowerCase();

    const isNifti =
      fileName.endsWith('.nii') ||
      fileName.endsWith('.nii.gz');

    if (!isNifti) {

      this.toastService.info(
       'Veuillez sélectionner un fichier .nii ou .nii.gz.', 
        'Format invalide'
      )

      return false;
    }

    return true;
  }


  removeFile(): void {

    if (this.isLoading) {
      return;
    }

    this.selectedFile = null;

    this.isDragging = false;

    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }


  formatFileSize(size: number): string {

    if (size === 0) {
      return '0 octet';
    }

    const units = [
      'octets',
      'Ko',
      'Mo',
      'Go'
    ];

    const index =
      Math.floor(
        Math.log(size) / Math.log(1024)
      );

    const value =
      size / Math.pow(1024, index);

    return `${value.toFixed(1)} ${units[index]}`;
  }


  startAnalysis(): void {

  if (
    this.selectedPatientId === null ||
    this.selectedFile === null ||
    this.isLoading
  ) {
    return;
  }

  this.isLoading = true;

  this.studiesService.analyzeStudy(
    this.selectedPatientId,
    this.selectedFile,
    'DWI'
  ).subscribe({
    next: (study) => {

      this.isLoading = false;

      console.log('Analyse réussie :', study);

      this.toastService.success(
        'L’analyse de l’IRM est terminée avec succès.',
        'Analyse terminée'
      );

      this.resetForm();
    },

    error: (error: HttpErrorResponse) => {

      this.isLoading = false;

      console.error(
        'Erreur lors de l’analyse :',
        error
      );

      if (error.status === 400) {
        this.toastService.error(
          error.error?.message || 'La requête est invalide.',
          'Requête invalide'
        );
        return;
      }

      if (error.status === 403) {
        this.toastService.error(
          error.error?.message ||
          'Vous n’avez pas l’autorisation d’analyser ce patient.',
          'Accès refusé'
        );
        return;
      }

      if (error.status === 404) {
        this.toastService.error(
          error.error?.message || 'Patient introuvable.',
          'Patient introuvable'
        );
        return;
      }

      if (error.status === 422) {
        this.toastService.error(
          error.error?.message ||
          'Le traitement de l’IRM a échoué.',
          'Échec de l’analyse'
        );
        return;
      }

      if (error.status === 500) {
        this.toastService.error(
          error.error?.message ||
          'Une erreur interne est survenue sur le serveur.',
          'Erreur serveur'
        );
        return;
      }

      this.toastService.error(
        'Impossible de terminer l’analyse.',
        'Erreur'
      );
    }
  });
}

private resetForm(): void {

    this.selectedPatientId = null;

    this.selectedPatient = null;

    this.removeFile();
  }

  cancel(): void {

    if (this.isLoading) {
      return;
    }

    this.selectedPatientId = null;

    this.selectedPatient = null;

    this.removeFile();
  }
}