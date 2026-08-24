import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { PatientService } from '../../../../services/patient.service';
import { Patient } from '../../../../models/patient/patient.model';
import { ToastService } from '../../../../services/toast.service';

@Component({
  selector: 'app-patient-list',
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.css'
})
export class PatientListComponent implements OnInit {

  showAddPatientModal = false;

  patients: Patient[] = [];

  loading = false;

  constructor(
    private patientService: PatientService,
    private toastService: ToastService, 
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPatients();
  }

  viewPatient(id: number): void {

  console.log('Opening patient:', id);

  this.router.navigate([
    '/dashboard',
    'patient',
    id
  ]);

}

  /**
   * Get all patients belonging to the authenticated doctor.
   *
   * GET /api/patient/by-docktor
   */
  loadPatients(): void {

    this.loading = true;

    this.patientService.getMyPatients().subscribe({

      next: (response) => {

        console.log('Patients retrieved successfully:', response);

        this.patients = response.patients ?? [];

        this.loading = false;
      },

      error: (error) => {

        console.error(
          'Error retrieving patients:',
          error
        );

        this.loading = false;

        this.handlePatientError(
          error,
          'Impossible de récupérer les patients.'
        );
      }

    });
  }

  onPatientCreated(): void {
  console.log('New patient created → refreshing list');
  this.loadPatients();
}

  /**
   * Open Add Patient modal.
   */
  openAddPatientModal(): void {

    this.showAddPatientModal = true;
  }

  /**
   * Close Add Patient modal.
   */
  closeAddPatientModal(): void {

    this.showAddPatientModal = false;
  }

  /**
   * Handle Patient API errors.
   */
  private handlePatientError(
    error: any,
    defaultMessage: string
  ): void {

    if (error?.status === 401) {

      this.toastService.error(
        'Votre session a expiré. Veuillez vous reconnecter.',
        'Session expirée'
      );

      return;
    }

    if (error?.status === 403) {

      this.toastService.error(
        'Vous n\'êtes pas autorisé à accéder aux patients.',
        'Accès refusé'
      );

      return;
    }

    if (error?.status === 404) {

      this.toastService.error(
        'Les patients demandés sont introuvables.',
        'Patients introuvables'
      );

      return;
    }

    if (error?.status === 500) {

      this.toastService.error(
        'Une erreur interne est survenue sur le serveur.',
        'Erreur serveur'
      );

      return;
    }

    this.toastService.error(
      defaultMessage,
      'Erreur'
    );
  }
}