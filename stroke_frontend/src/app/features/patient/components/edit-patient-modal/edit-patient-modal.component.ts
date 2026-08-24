import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges
} from '@angular/core';

import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators
} from '@angular/forms';

import { Subscription } from 'rxjs';

import { PatientService } from '../../../../services/patient.service';
import { ToastService } from '../../../../services/toast.service';

import {
  Patient,
  UpdatePatientRequest
} from '../../../../models/patient/patient.model';


@Component({
  selector: 'app-edit-patient-modal',
  templateUrl: './edit-patient-modal.component.html',
  styleUrl: './edit-patient-modal.component.css'
})
export class EditPatientModalComponent
  implements OnChanges, OnDestroy {

  @Input()
  patientId!: number;

  @Output()
  close = new EventEmitter<void>();

  @Output()
  patientUpdated = new EventEmitter<void>();


  patient: Patient | null = null;

  patientForm: FormGroup;

  submitted = false;

  isLoading = false;

  isLoadingPatient = false;


  private patientSubscription?: Subscription;


  constructor(
    private fb: FormBuilder,
    private patientService: PatientService,
    private toastService: ToastService
  ) {

    this.patientForm = this.fb.group({

      patientCode: [
        {
          value: '',
          disabled: true
        }
      ],

      firstName: [
        '',
        [
          Validators.required,
          Validators.minLength(2)
        ]
      ],

      lastName: [
        '',
        [
          Validators.required,
          Validators.minLength(2)
        ]
      ],

      dateOfBirth: [
        '',
        [
          Validators.required,
          this.validDateOfBirth
        ]
      ],

      sex: [
        'MALE',
        [
          Validators.required
        ]
      ],

      age: [
        {
          value: null,
          disabled: true
        }
      ],

      weight: [
        null,
        [
          Validators.required,
          Validators.min(0)
        ]
      ],

      phoneNumber: [
        '',
        [
          Validators.required
        ]
      ]

    });

  }


  ngOnChanges(
    changes: SimpleChanges
  ): void {

    if (
      changes['patientId'] &&
      this.patientId
    ) {

      this.loadPatient();

    }

  }


  /**
   * GET /api/patient/{id}
   */
  loadPatient(): void {

    this.isLoadingPatient = true;

    this.patient = null;

    this.patientForm.reset({
      patientCode: '',
      firstName: '',
      lastName: '',
      dateOfBirth: '',
      sex: 'MALE',
      age: null,
      weight: null,
      phoneNumber: ''
    });

    this.patientSubscription =
      this.patientService
        .getPatientById(this.patientId)
        .subscribe({

          next: (response) => {

            this.patient =
              response.patient;

            this.patientForm.patchValue({

              patientCode:
                this.patient.patientCode,

              firstName:
                this.patient.firstName,

              lastName:
                this.patient.lastName,

              dateOfBirth:
                this.patient.dateOfBirth,

              sex:
                this.patient.sex,

              age:
                this.patient.age,

              weight:
                this.patient.weight,

              phoneNumber:
                this.patient.phoneNumber

            });

            this.isLoadingPatient = false;

          },

          error: (error) => {

            console.error(
              'Error retrieving patient:',
              error
            );

            this.isLoadingPatient = false;

            this.handleError(
              error,
              'Impossible de récupérer les informations du patient.'
            );

          }

        });

  }


  /**
   * Calculate age automatically.
   */
  onDateOfBirthChange(): void {

    const dateOfBirth =
      this.patientForm
        .get('dateOfBirth')
        ?.value;

    if (!dateOfBirth) {

      this.patientForm
        .get('age')
        ?.setValue(null);

      return;
    }

    const age =
      this.calculateAge(dateOfBirth);

    this.patientForm
      .get('age')
      ?.setValue(age);

  }


  private calculateAge(
    dateOfBirth: string
  ): number {

    const birthDate =
      new Date(dateOfBirth);

    const today =
      new Date();

    let age =
      today.getFullYear() -
      birthDate.getFullYear();

    const month =
      today.getMonth() -
      birthDate.getMonth();

    if (
      month < 0 ||
      (
        month === 0 &&
        today.getDate() <
        birthDate.getDate()
      )
    ) {

      age--;

    }

    return age;

  }


  /**
   * Custom date validator.
   */
  private validDateOfBirth(
    control: AbstractControl
  ): ValidationErrors | null {

    if (!control.value) {
      return null;
    }

    const selectedDate =
      new Date(control.value);

    const today =
      new Date();

    today.setHours(0, 0, 0, 0);

    if (selectedDate > today) {

      return {
        futureDate: true
      };

    }

    return null;

  }


  /**
   * Save changes.
   *
   * PUT /api/patient/{id}
   */
  savePatient(): void {

    this.submitted = true;

    if (
      this.patientForm.invalid ||
      this.isLoading
    ) {

      this.patientForm.markAllAsTouched();

      return;

    }


    this.isLoading = true;


    const request: UpdatePatientRequest = {

      firstName:
        this.patientForm
          .get('firstName')
          ?.value,

      lastName:
        this.patientForm
          .get('lastName')
          ?.value,

      dateOfBirth:
        this.patientForm
          .get('dateOfBirth')
          ?.value,

      sex:
        this.patientForm
          .get('sex')
          ?.value,

      age:
        this.calculateAge(
          this.patientForm
            .get('dateOfBirth')
            ?.value
        ),

      weight:
        Number(
          this.patientForm
            .get('weight')
            ?.value
        ),

      phoneNumber:
        this.patientForm
          .get('phoneNumber')
          ?.value

    };


    this.patientService
      .updatePatient(
        this.patientId,
        request
      )
      .subscribe({

        next: (response) => {

          console.log(
            'Patient updated:',
            response
          );

          this.isLoading = false;

          this.toastService.success(
            'Les informations du patient ont été mises à jour.',
            'Patient modifié'
          );

          this.patientUpdated.emit();

          this.close.emit();

        },

        error: (error) => {

          console.error(
            'Error updating patient:',
            error
          );

          this.isLoading = false;

          this.handleError(
            error,
            'Impossible de modifier le patient.'
          );

        }

      });

  }


  closeModal(): void {

    if (this.isLoading) {
      return;
    }

    this.close.emit();

  }


  private handleError(
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
        'Vous n\'êtes pas autorisé à modifier ce patient.',
        'Accès refusé'
      );

      return;

    }


    if (error?.status === 404) {

      this.toastService.error(
        'Patient introuvable.',
        'Patient introuvable'
      );

      return;

    }


    if (error?.status === 400) {

      this.toastService.error(
        'Les informations saisies sont invalides.',
        'Données invalides'
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


  ngOnDestroy(): void {

    this.patientSubscription?.unsubscribe();

  }

}