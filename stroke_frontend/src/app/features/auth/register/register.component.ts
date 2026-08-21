import { Component } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators
} from '@angular/forms';

import { NavigationService } from '../../../services/navigation.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {

  registerForm: FormGroup;

  submitted = false;
  registrationError = false;

  showPassword = false;
  showConfirmPassword = false;

  passwordStrength = 0;

  readonly roles = [
    'Radiologist',
    'Neurologist',
    'General Practitioner',
    'Medical Administrator'
  ];

  constructor(private fb: FormBuilder, private navigation: NavigationService) {

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


  /* ==========================================================
     GETTERS
     ========================================================== */

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


  /* ==========================================================
     PASSWORD MATCH VALIDATOR
     ========================================================== */

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


  /* ==========================================================
     FORM SUBMISSION
     ========================================================== */

  onSubmit(): void {

    this.submitted = true;
    this.registrationError = false;

    if (this.registerForm.invalid) {

      this.registerForm.markAllAsTouched();

      return;
    }

    const registrationData = {
      firstName: this.registerForm.value.firstName,
      lastName: this.registerForm.value.lastName,
      email: this.registerForm.value.email,
      role: this.registerForm.value.role,
      institution: this.registerForm.value.institution,
      password: this.registerForm.value.password
    };

    console.log(
      'Registration data:',
      registrationData
    );

    /*
     * Later:
     *
     * this.authService.register(registrationData).subscribe({
     *
     *   next: () => {
     *      this.router.navigate(['/auth/login']);
     *   },
     *
     *   error: () => {
     *      this.registrationError = true;
     *   }
     *
     * });
     */
  }


  /* ==========================================================
     PASSWORD VISIBILITY
     ========================================================== */

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword =
      !this.showConfirmPassword;
  }


  /* ==========================================================
     PASSWORD STRENGTH
     ========================================================== */

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