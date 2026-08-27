import { Component, Input, OnInit} from '@angular/core';
import { Study } from '../../../../models/studies/studies.model';
import { StudiesService } from '../../../../services/studies.service';

@Component({
  selector: 'app-patient-studies',
  templateUrl: './patient-studies.component.html',
  styleUrl: './patient-studies.component.css'
})
export class PatientStudiesComponent implements OnInit {

  @Input()
  patientId!: number;

  studies: Study[] = [];

  loading = false;

  error = false;


  constructor(
    private studiesService: StudiesService
  ) {}


  ngOnInit(): void {

    if (!this.patientId) {

      console.error(
        'Patient ID is required.'
      );

      return;
    }

    this.loadStudies();
  }


  /**
   * Récupère toutes les études du patient.
   *
   * GET /api/studies/patients/{patientId}
   */
  loadStudies(): void {

    this.loading = true;
    this.error = false;

    console.log(
      'Loading studies for patient:',
      this.patientId
    );

    this.studiesService
      .getStudiesByPatient(this.patientId)
      .subscribe({

        next: (response) => {

          console.log(
            'Studies retrieved successfully:',
            response
          );

          this.studies =
            response.studies ?? [];

          this.loading = false;
        },


        error: (error) => {

          console.error(
            'Error retrieving studies:',
            error
          );

          this.studies = [];

          this.loading = false;

          this.error = true;
        }

      });
  }


  /**
   * Nombre total d'études.
   */
  get studyCount(): number {

    return this.studies.length;
  }


  /**
   * Retourne le libellé du statut.
   */
  getStatusLabel(status: string): string {

    switch (status) {

      case 'COMPLETED':
        return 'Terminée';

      case 'UPLOADED':
        return 'Importée';

      case 'PROCESSING':
        return 'En traitement';

      case 'PENDING':
        return 'En attente';

      case 'FAILED':
        return 'Échec';

      default:
        return status;
    }
  }


  /**
   * Retourne la classe CSS du statut.
   */
  getStatusClass(status: string): string {

    switch (status) {

      case 'COMPLETED':
        return 'status-completed';

      case 'PROCESSING':
        return 'status-processing';

      case 'PENDING':
      case 'UPLOADED':
        return 'status-pending';

      case 'FAILED':
        return 'status-failed';

      default:
        return 'status-default';
    }
  }


  /**
   * Retourne le résultat de l'analyse IA.
   */
  getLesionLabel(study: Study): string {

    if (!study.prediction) {

      return study.status === 'COMPLETED'
        ? 'Résultat disponible'
        : 'En attente';
    }

    return study.prediction.lesionDetected
      ? 'Lésion détectée'
      : 'Aucune lésion';
  }


  /**
   * Retourne la classe CSS du résultat.
   */
  getLesionClass(study: Study): string {

    if (!study.prediction) {

      return 'result-pending';
    }

    return study.prediction.lesionDetected
      ? 'result-lesion'
      : 'result-no-lesion';
  }


  /**
   * Ouvre le détail d'une étude.
   *
   * La navigation sera ajoutée ensuite.
   */
  viewStudy(study: Study): void {

    console.log(
      'Selected study:',
      study
    );
  }

}