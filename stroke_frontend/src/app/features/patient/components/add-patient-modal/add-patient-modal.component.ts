import {Component, EventEmitter,Output} from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators} from '@angular/forms';

import { PatientService } from '../../../../services/patient.service';
import { ToastService } from '../../../../services/toast.service';

import {CreatePatientRequest} from '../../../../models/patient/patient.model';

@Component({
  selector: 'app-add-patient-modal',
  templateUrl: './add-patient-modal.component.html',
  styleUrls: ['./add-patient-modal.component.css']
})
export class AddPatientModalComponent {

  @Output() close = new EventEmitter<void>();
  @Output() patientCreated = new EventEmitter<void>();

  patientForm: FormGroup;

  submitted = false;

  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private patientService: PatientService,
    private toastService: ToastService
  ) {

    this.patientForm = this.fb.group({

      patientCode: [
        '',
        [
          Validators.required,
          Validators.maxLength(50)
        ]
      ],

      firstName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100)
        ]
      ],

      lastName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(100)
        ]
      ],

      dateOfBirth: [
        '',
        [
          Validators.required,
          this.notFutureDateValidator
        ]
      ],

      sex: [
        'MALE',
        Validators.required
      ],

      age: [
        null,
        [
          Validators.required,
          Validators.min(0),
          Validators.max(120)
        ]
      ],

      weight: [
        null,
        [
          Validators.required,
          Validators.min(1),
          Validators.max(500)
        ]
      ],

      phoneNumber: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\+?[0-9\s\-()]{8,20}$/)
        ]
      ]

    });
  }

  /**
   * Convenient getter for form controls.
   */
  get f(): {
    [key: string]: AbstractControl
  } {
    return this.patientForm.controls;
  }

  /**
   * Prevent a future date of birth.
   */
  notFutureDateValidator(
    control: AbstractControl
  ): ValidationErrors | null {

    if (!control.value) {
      return null;
    }

    const selectedDate =
      new Date(control.value);

    const today = new Date();

    today.setHours(0, 0, 0, 0);

    return selectedDate > today
      ? { futureDate: true }
      : null;
  }

  /**
   * Save patient.
   *
   * POST /api/patient
   */
  savePatient(): void {

    this.submitted = true;

    /*
     * Do not send the request if the form
     * is invalid.
     */
    if (this.patientForm.invalid) {

      this.patientForm.markAllAsTouched();

      return;
    }

    /*
     * Prevent double click / duplicate requests.
     */
    if (this.isLoading) {
      return;
    }

    this.isLoading = true;

    const patient: CreatePatientRequest = {
      patientCode:
        this.patientForm.value.patientCode,

      firstName:
        this.patientForm.value.firstName,

      lastName:
        this.patientForm.value.lastName,

      dateOfBirth:
        this.patientForm.value.dateOfBirth,

      sex:
        this.patientForm.value.sex,

      age:
        Number(this.patientForm.value.age),

      weight:
        Number(this.patientForm.value.weight),

      phoneNumber:
        this.patientForm.value.phoneNumber
    };

    console.log(
      'Creating patient:',
      patient
    );

    this.patientService
      .createPatient(patient)
      .subscribe({

        /**
         * HTTP 201
         */
        next: (response) => {

          console.log(
            'Patient created successfully:',
            response
          );

          this.isLoading = false;

          this.toastService.success(
            'Le patient a été ajouté avec succès.',
            'Patient créé'
          );

          /*
           * Tell PatientListComponent that
           * a new patient was created.
           */
          this.patientCreated.emit();

          /*
           * Close modal only after successful
           * backend response.
           */
          this.closeModal();
        },

        /**
         * HTTP error
         */
        error: (error) => {

          console.error(
            'Error creating patient:',
            error
          );

          this.isLoading = false;

          this.handleCreatePatientError(error);
        }

      });
  }

  /**
   * Handle errors returned by the Patient API.
   */
  private handleCreatePatientError(
    error: any
  ): void {

    /*
     * 400 Bad Request
     */
    if (error?.status === 400) {

      this.toastService.error(
        'Les informations saisies sont invalides.',
        'Données invalides'
      );

      return;
    }

    /*
     * 401 Unauthorized
     */
    if (error?.status === 401) {

      this.toastService.error(
        'Votre session a expiré. Veuillez vous reconnecter.',
        'Session expirée'
      );

      return;
    }

    /*
     * 403 Forbidden
     */
    if (error?.status === 403) {

      this.toastService.error(
        'Vous n\'êtes pas autorisé à créer un patient.',
        'Accès refusé'
      );

      return;
    }

    /*
     * 409 Conflict
     *
     * Patient code already exists.
     */
    if (error?.status === 409) {

      this.toastService.error(
        'Ce code patient existe déjà. Veuillez utiliser un autre code.',
        'Code patient existant'
      );

      return;
    }

    /*
     * 500 Internal Server Error
     */
    if (error?.status === 500) {

      this.toastService.error(
        'Une erreur interne est survenue sur le serveur.',
        'Erreur serveur'
      );

      return;
    }

    /*
     * Unknown error
     */
    this.toastService.error(
      'Impossible de créer le patient. Veuillez réessayer.',
      'Erreur'
    );
  }

  /**
   * Close modal.
   */
  closeModal(): void {

    if (this.isLoading) {
      return;
    }

    this.close.emit();
  }
}