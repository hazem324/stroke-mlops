import {
  Component,
  OnInit
} from '@angular/core';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import { PatientService } from '../../../../services/patient.service';
import { ToastService } from '../../../../services/toast.service';

import { Patient } from '../../../../models/patient/patient.model';

@Component({
  selector: 'app-patient-detail-page',
  templateUrl: './patient-detail-page.component.html',
  styleUrl: './patient-detail-page.component.css'
})
export class PatientDetailPageComponent implements OnInit {

  patient: Patient | null = null;

  patientId!: number;

  loading = false;


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private patientService: PatientService,
    private toastService: ToastService
  ) {}


  ngOnInit(): void {

    const id =
      this.route.snapshot.paramMap.get('id');

    if (!id) {

      this.toastService.error(
        'Identifiant du patient introuvable.',
        'Erreur'
      );

      this.router.navigate([
        '/patients'
      ]);

      return;
    }

    this.patientId = Number(id);

    this.loadPatient();
  }


  /**
   * GET /api/patient/{id}
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


  /**
   * Navigate to edit patient.
   */
  editPatient(): void {

    this.router.navigate([
      '/patients',
      this.patientId,
      'edit'
    ]);
  }


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


  /**
   * Return to patient list.
   */
  backToPatients(): void {

    this.router.navigate([
      '/patients'
    ]);
  }


  /**
   * Handle API errors.
   */
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
        '/patients'
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

}