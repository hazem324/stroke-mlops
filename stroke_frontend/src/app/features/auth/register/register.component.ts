import { Component } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators
} from '@angular/forms';

import { NavigationService } from '../../../services/navigation.service';
import { AuthService } from '../../../services/auth.service';

import { RegisterRequest } from '../../../models/auth/register-request.model';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {

  registerForm: FormGroup;

  submitted = false;
  registrationError = false;

  showPassword = false;
  showConfirmPassword = false;
  isLoading = false;

  passwordStrength = 0;

  readonly roles = [
    /*'Radiologist',
    'Neurologist',
    'General Practitioner',
    'Medical Administrator'*/
    'ADMIN',
    'DOCTOR',
    'RADIOLOGIST'
  ];

  constructor(private fb: FormBuilder, private navigation: NavigationService, private authService: AuthService, private toast: ToastService) {

    this.registerForm = this.fb.group(
      {
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

        email: [
          '',
          [
            Validators.required,
            Validators.email
          ]
        ],

        role: [
          '',
          Validators.required
        ],

        institution: [
          ''
        ],

        password: [
          '',
          [
            Validators.required,
            Validators.minLength(8)
          ]
        ],

        confirmPassword: [
          '',
          Validators.required
        ],

        terms: [
          false,
          Validators.requiredTrue
        ]
      },
      {
        validators: this.passwordMatchValidator
      }
    );
  }

  get firstName() {
    return this.registerForm.get('firstName');
  }

  get lastName() {
    return this.registerForm.get('lastName');
  }

  get email() {
    return this.registerForm.get('email');
  }

  get role() {
    return this.registerForm.get('role');
  }

  get institution() {
    return this.registerForm.get('institution');
  }

  get password() {
    return this.registerForm.get('password');
  }

  get confirmPassword() {
    return this.registerForm.get('confirmPassword');
  }

  get terms() {
    return this.registerForm.get('terms');
  }

  private passwordMatchValidator(
    control: AbstractControl
  ): ValidationErrors | null {

    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;

    if (!password || !confirmPassword) {
      return null;
    }

    return password === confirmPassword
      ? null
      : { passwordMismatch: true };
  }

  onSubmit(): void {

  this.submitted = true;
  this.registrationError = false;

  if (this.registerForm.invalid) {

    this.registerForm.markAllAsTouched();

    this.toast.error(
      'Veuillez corriger les champs indiqués ci-dessous.',
      'Formulaire invalide'
    );

    return;
  }

  if (!this.registerForm.value.terms) {

    this.terms?.markAsTouched();

    this.toast.error(
      'Vous devez accepter les conditions d’utilisation.',
      'Conditions requises'
    );

    return;
  }

  if (this.isLoading) {
    return;
  }

  const registrationData: RegisterRequest = {

    firstName:
      this.registerForm.value.firstName.trim(),

    lastName:
      this.registerForm.value.lastName.trim(),

    email:
      this.registerForm.value.email.trim().toLowerCase(),

    password:
      this.registerForm.value.password,

    role:
      this.registerForm.value.role,

    // Frontend "institution"
    // Backend "establishment"
    establishment:
      this.registerForm.value.institution?.trim() || '',

    // Frontend "terms"
    // Backend "acceptedTerms"
    acceptedTerms:
      this.registerForm.value.terms
  };


  console.log(
    'Registration data:',
    registrationData
  );

  this.isLoading = true;

  this.authService.register(registrationData).subscribe({

    next: (response) => {

      this.registrationError = false;
      this.isLoading = false;

      this.toast.success(
        response.message || 'Compte créé avec succès',
        'Inscription'
      );

      // Redirect to login
      this.navigation.goToLogin();
    },

    error: (error) => {

      this.registrationError = true;
      this.isLoading = false;

      const message =
        error?.error?.message ||
        'Une erreur est survenue lors de la création du compte.';


      switch (error?.status) {

        case 400:

          this.toast.error(
            message,
            'Informations invalides'
          );

          break;

        case 409:

          this.toast.error(
            message,
            'Email déjà utilisé'
          );

          break;

        case 500:

          this.toast.error(
            'Une erreur interne est survenue. Veuillez réessayer plus tard.',
            'Erreur serveur'
          );

          break;

        default:

          this.toast.error(
            message,
            'Erreur'
          );

          break;
      }
    }
  });
}

  canSubmit(): boolean {
    return !!this.terms?.value && !this.isLoading;
  }


    /* PASSWORD VISIBILITY */ 

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword =
      !this.showConfirmPassword;
  }


     /* PASSWORD STRENGTH */

  onPasswordChange(): void {

    const value = this.password?.value || '';

    let score = 0;

    if (value.length >= 8) {
      score++;
    }

    if (/[A-Z]/.test(value)) {
      score++;
    }

    if (/[0-9]/.test(value)) {
      score++;
    }

    if (/[^A-Za-z0-9]/.test(value)) {
      score++;
    }

    this.passwordStrength = score;
  }


  getPasswordStrengthLabel(): string {

    if (!this.password?.value) {
      return '8 caractères minimum, avec majuscule et chiffre.';
    }

    const labels = [
      'Faible',
      'Moyen',
      'Bon',
      'Excellent'
    ];

    return `Force du mot de passe : ${
      labels[Math.max(this.passwordStrength - 1, 0)]
    }`;
  }


  getPasswordStrengthClass(): string {

    switch (this.passwordStrength) {

      case 1:
        return 'strength-weak';

      case 2:
        return 'strength-medium';

      case 3:
        return 'strength-good';

      case 4:
        return 'strength-excellent';

      default:
        return '';
    }
  }


  /* ==========================================================
     VALIDATION HELPER
     ========================================================== */

  isInvalid(controlName: string): boolean {

    const control =
      this.registerForm.get(controlName);

    return !!(
      control &&
      control.invalid &&
      (control.touched || this.submitted)
    );
  }


  isPasswordMismatch(): boolean {

    return !!(
      this.registerForm.hasError('passwordMismatch') &&
      (this.confirmPassword?.touched || this.submitted)
    );
  }

  /* navigation */ 
  goToLogin(): void {
    this.navigation.goToLogin();
  }

}