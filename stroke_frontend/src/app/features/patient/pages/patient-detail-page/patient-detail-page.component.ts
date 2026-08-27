import {
  Component,
  Input,
  OnInit
} from '@angular/core';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import { PatientService } from '../../../../services/patient.service';
import { ToastService } from '../../../../services/toast.service';

import { Patient } from '../../../../models/patient/patient.model';
import { StudiesService } from '../../../../services/studies.service';

@Component({
  selector: 'app-patient-detail-page',
  templateUrl: './patient-detail-page.component.html',
  styleUrl: './patient-detail-page.component.css'
})
export class PatientDetailPageComponent implements OnInit {

  studyCount = 0;

  patient: Patient | null = null;
  
  patientId!: number;

  // =========================================================
  // LOADING
  // =========================================================

  loading = false;


  // =========================================================
  // EDIT MODAL
  // =========================================================

  showEditPatientModal = false;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private studiesService: StudiesService,
    private route: ActivatedRoute,
    private router: Router,
    private patientService: PatientService,
    private toastService: ToastService
  ) {}


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {

    const id =
      this.route.snapshot.paramMap.get('id');

    if (!id) {

      this.toastService.error(
        'Identifiant du patient introuvable.',
        'Erreur'
      );

      this.router.navigate([
        '/dashboard/patient'
      ]);

      return;
    }

    this.patientId = Number(id);

    // Vérification de l'ID
    if (isNaN(this.patientId)) {

      this.toastService.error(
        'Identifiant du patient invalide.',
        'Erreur'
      );

      this.router.navigate([
        '/dashboard/patient'
      ]);

      return;
    }

    this.loadPatient();

    this.loadStudyCount();
  }

onStudyCountChange(count: number): void {
  this.studyCount = count;
}

  // =========================================================
  // GET PATIENT
  // =========================================================

  /**
   * GET /api/patient/{id}
   *
   * Récupère les informations du patient.
   */
  loadPatient(): void {

    this.loading = true;

    this.patientService
      .getPatientById(this.patientId)
      .subscribe({

        next: (response) => {

          console.log(
            'Patient retrieved successfully:',
            response
          );

          this.patient =
            response.patient;

          this.loading = false;
        },


        error: (error) => {

          console.error(
            'Error retrieving patient:',
            error
          );

          this.loading = false;

          this.handleError(error);
        }

      });
  }


  // =========================================================
  // EDIT PATIENT
  // =========================================================

  /**
   * Ouvre le même modal de modification
   * utilisé dans la liste des patients.
   */
  editPatient(): void {

    console.log(
      'Opening edit modal for patient:',
      this.patientId
    );

    this.showEditPatientModal = true;
  }


  /**
   * Ferme le modal de modification.
   */
  closeEditPatientModal(): void {

    this.showEditPatientModal = false;
  }


  /**
   * Appelé après une modification réussie.
   *
   * Recharge les informations du patient
   * sans recharger le navigateur.
   */
  onPatientUpdated(): void {

    console.log(
      'Patient updated → refreshing patient details'
    );

    this.showEditPatientModal = false;

    this.loadPatient();
  }


  // =========================================================
  // NEW ANALYSIS
  // =========================================================

  /**
   * Navigate to new MRI analysis.
   */
  newAnalysis(): void {

    this.router.navigate(
      ['/new-analysis'],
      {
        queryParams: {
          patientId: this.patientId
        }
      }
    );
  }


  // =========================================================
  // RETURN
  // =========================================================

  /**
   * Return to patient list.
   */
  backToPatients(): void {

    this.router.navigate([
      '/dashboard/patient'
    ]);
  }


  // =========================================================
  // ERROR HANDLING
  // =========================================================

  private handleError(
    error: any
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
        'Vous n\'êtes pas autorisé à accéder à ce patient.',
        'Accès refusé'
      );

      return;
    }


    if (error?.status === 404) {

      this.toastService.error(
        'Patient introuvable.',
        'Patient introuvable'
      );

      this.router.navigate([
        '/dashboard/patient'
      ]);

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
      'Impossible de récupérer les informations du patient.',
      'Erreur'
    );
  }

  loadStudyCount(): void {

  this.studiesService
    .getStudiesByPatient(this.patientId)
    .subscribe({

      next: (response) => {

        this.studyCount =
          response.studies?.length ?? 0;

      },

      error: (error) => {

        console.error(
          'Error retrieving study count:',
          error
        );

        this.studyCount = 0;
      }

    });
}
}