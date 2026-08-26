import {Component, ElementRef, ViewChild, OnInit} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { PatientService } from '../../../../services/patient.service';
import { Patient } from '../../../../models/patient/patient.model';
import { ToastService } from '../../../../services/toast.service';


@Component({
  selector: 'app-study-analysis',
  templateUrl: './study-analysis.component.html',
  styleUrls: ['./study-analysis.component.css']
})
export class StudyAnalysisComponent implements OnInit {

  constructor(private patientService: PatientService, private toastService:ToastService){}

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
  isProcessing = false;
  currentProcessingStep = -1;

  processingSteps: string[] = [
    'Téléchargement de l’IRM…',
    'Traitement de l’IRM…',
    'Exécution du modèle IA…',
    'Génération de la segmentation…',
    'Enregistrement du résultat…'
  ];


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

    if (this.isProcessing) {
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

    if (this.isProcessing) {
      return;
    }

    this.isDragging = true;
  }


  onDragOver(event: DragEvent): void {

    event.preventDefault();

    if (this.isProcessing) {
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

    if (this.isProcessing) {
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

      alert(
        'Format invalide. Veuillez sélectionner un fichier .nii ou .nii.gz.'
      );

      return false;
    }

    return true;
  }


  removeFile(): void {

    if (this.isProcessing) {
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
      !this.selectedPatientId ||
      !this.selectedFile ||
      this.isProcessing
    ) {
      return;
    }

    this.isProcessing = true;

    this.currentProcessingStep = 0;

    this.runProcessingStep();
  }


  private runProcessingStep(): void {

    if (
      this.currentProcessingStep >=
      this.processingSteps.length
    ) {

      this.finishProcessing();

      return;
    }

    setTimeout(() => {

      this.currentProcessingStep++;

      this.runProcessingStep();

    }, 850);
  }


  private finishProcessing(): void {

    this.isProcessing = false;

    this.currentProcessingStep =
      this.processingSteps.length;

    console.log('Analyse terminée.');

    console.log(
      'Patient :',
      this.selectedPatient
    );

    console.log(
      'Fichier :',
      this.selectedFile
    );

    /*
     * L'appel à StudiesService.analyzeStudy()
     * sera ajouté ici dans l'étape suivante.
     */
  }


  cancel(): void {

    if (this.isProcessing) {
      return;
    }

    this.selectedPatientId = null;

    this.selectedPatient = null;

    this.removeFile();
  }
}