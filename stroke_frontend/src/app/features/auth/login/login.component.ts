import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavigationService } from '../../../services/navigation.service';
import { ToastService } from '../../../services/toast.service';
import { AuthService } from '../../../services/auth.service';
import { LoginRequest } from '../../../models/auth/login-request.model';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  loginForm: FormGroup;

  submitted = false;
  authenticationError = false;
  showPassword = false;
  isLoading = false;

  constructor(private fb: FormBuilder, private navigationService: NavigationService, private toast: ToastService, private authService: AuthService) {

    this.loginForm = this.fb.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6)
        ]
      ],
      rememberMe: [true]
    });
  }

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }

  onSubmit(): void {

    this.submitted = true;
    this.authenticationError = false;

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.toast.error(
        'Veuillez vérifier les informations saisies.',
        'Formulaire invalide'
      );
      return;
    }

    // Prevent double submit while a request is already in flight
    if (this.isLoading) {
      return;
    }

    const credentials: LoginRequest = {
      email: this.loginForm.value.email.trim(),
      password: this.loginForm.value.password
    };

    this.isLoading = true;   // <-- START LOADING

    this.authService.login(credentials).subscribe({

      next: (response) => {
        this.authenticationError = false;

        this.toast.success(
          response.message || 'Connexion réussie',
          'Bienvenue'
        );

        this.goToHomeDash();

        // Keep isLoading = true here since we're navigating away.
        // If you prefer to reset it immediately, uncomment below:
        // this.isLoading = false;
      },

      error: (error) => {
        console.error('Login error:', error);

        this.authenticationError = true;
        this.isLoading = false;   // <-- STOP LOADING ON ERROR

        const message =
          error?.error?.message ||
          'Une erreur est survenue lors de la connexion.';

        switch (error?.status) {
          case 400:
            this.toast.error(message, 'Informations invalides');
            break;
          case 401:
            this.toast.error(message, 'Connexion échouée');
            break;
          case 403:
            this.toast.error(message, 'Compte désactivé');
            break;
          case 500:
            this.toast.error(
              'Une erreur interne est survenue. Veuillez réessayer plus tard.',
              'Erreur serveur'
            );
            break;
          default:
            this.toast.error(message, 'Erreur');
            break;
        }
      }
    });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  isInvalid(controlName: string): boolean {
    const control = this.loginForm.get(controlName);

    return !!(
      control &&
      control.invalid &&
      (control.touched || this.submitted)
    );
  }

  /* navigation */ 
  goToSignUp () :void {
    this.navigationService.goToRegister();
  } 

  goToPatient (): void {
    this.navigationService.goToPatient();
  }

  goToHomeDash(): void{
    this.navigationService.goToHomeDash();
  }


  testSuccess(): void {
    this.toast.success(
      'Connexion réussie',
      'Succès'
    );
  }

  testError(): void {
    this.toast.error(
      'Email ou mot de passe incorrect',
      'Erreur'
    );
  }

  testInfo(): void {
    this.toast.info(
      'Veuillez vérifier vos informations',
      'Information'
    );
  }

  testWarning(): void {
    this.toast.warning(
      'Votre session va bientôt expirer',
      'Attention'
    );
  }

}