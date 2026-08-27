import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { PatientService } from '../../../../services/patient.service';
import { Patient } from '../../../../models/patient/patient.model';
import { ToastService } from '../../../../services/toast.service';
import { StudiesService } from '../../../../services/studies.service';
import { Study } from '../../../../models/studies/studies.model';


interface PatientRow extends Patient {
  studyCount: number;
  latestAnalysis: string | null;
}


@Component({
  selector: 'app-patient-list',
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.css'
})
export class PatientListComponent implements OnInit {

  showAddPatientModal = false;
  showEditPatientModal = false;

  selectedPatientId: number | null = null;

  patients: PatientRow[] = [];

  loading = false;

  constructor(
    private studiesService: StudiesService,
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

      const patients = response.patients ?? [];

      console.log(
        'Patients retrieved successfully:',
        patients
      );

      if (patients.length === 0) {

        this.patients = [];
        this.loading = false;

        return;
      }

      this.loadStudiesForPatients(patients);
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

loadStudiesForPatients(
  patients: Patient[]
): void {

  const requests = patients.map(
    patient =>
      this.studiesService.getStudiesByPatient(patient.id)
  );

  let completedRequests = 0;

  const patientRows: PatientRow[] = patients.map(
    patient => ({
      ...patient,
      studyCount: 0,
      latestAnalysis: null
    })
  );

  requests.forEach(
    (request, index) => {

      request.subscribe({

        next: (response) => {

          const studies =
            response.studies ?? [];

          patientRows[index].studyCount = studies.length;
          patientRows[index].latestAnalysis = this.getLatestAnalysis(studies);

          completedRequests++;

          if (completedRequests === patients.length) {

            this.patients = patientRows;

            this.loading = false;

            console.log(
              'Patients with study information:',
              this.patients
            );
          }
        },

        error: (error) => {

          console.error(
            `Error retrieving studies for patient ${patients[index].id}:`,
            error
          );

          completedRequests++;

          if (completedRequests === patients.length) {

            this.patients = patientRows;

            this.loading = false;
          }
        }

      });
    }
  );
}

getLatestAnalysis(studies: Study[]): string | null {

  if (!studies || studies.length === 0) {
    return null;
  }

  const latestStudy = studies.reduce(
    (latest, current) => {

      const latestDate =
        new Date(latest.updatedAt).getTime();

      const currentDate =
        new Date(current.updatedAt).getTime();

      return currentDate > latestDate
        ? current
        : latest;
    }
  );

  return latestStudy.updatedAt;
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
 * Ouvre le modal de modification
 * avec l'ID du patient sélectionné.
 */
openEditPatientModal(id: number): void {

  console.log('Opening edit patient modal:', id);

  this.selectedPatientId = id;
  this.showEditPatientModal = true;
}


/**
 * Ferme le modal de modification.
 */
closeEditPatientModal(): void {

  this.showEditPatientModal = false;
  this.selectedPatientId = null;
}


/**
 * Appelé après une modification réussie.
 * Recharge la liste sans recharger la page.
 */
onPatientUpdated(): void {

  console.log('Patient updated → refreshing list');

  this.showEditPatientModal = false;
  this.selectedPatientId = null;

  this.loadPatients();
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