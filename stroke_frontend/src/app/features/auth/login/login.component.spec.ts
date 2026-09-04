import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { LoginComponent } from './login.component';
import { AuthService } from '../../../services/auth.service';
import { NavigationService } from '../../../services/navigation.service';
import { ToastService } from '../../../services/toast.service';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let authService: jasmine.SpyObj<AuthService>;
  let toastService: jasmine.SpyObj<ToastService>;
  let navigationService: jasmine.SpyObj<NavigationService>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj('AuthService', ['login']);
    authService.login.and.returnValue(of({ message: 'Connexion réussie', token: 'jwt-token', expiresIn: 3600 }));
    toastService = jasmine.createSpyObj('ToastService', ['success', 'error', 'info', 'warning']);
    navigationService = jasmine.createSpyObj('NavigationService', ['goToRegister', 'goToPatient', 'goToHomeDash']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: ToastService, useValue: toastService },
        { provide: NavigationService, useValue: navigationService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render form and allow valid submit', () => {
    component.loginForm.setValue({
      email: 'doctor@example.com',
      password: 'Password1',
      rememberMe: true
    });

    component.onSubmit();

    expect(authService.login).toHaveBeenCalled();
    expect(toastService.success).toHaveBeenCalled();
    expect(navigationService.goToHomeDash).toHaveBeenCalled();
  });

  it('should validate invalid form before submitting', () => {
    component.loginForm.setValue({ email: '', password: '', rememberMe: true });

    component.onSubmit();

    expect(authService.login).not.toHaveBeenCalled();
    expect(toastService.error).toHaveBeenCalled();
  });
});
