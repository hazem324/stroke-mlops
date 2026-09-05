import { FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let auth: jasmine.SpyObj<any>;
  let navigation: jasmine.SpyObj<any>;
  let toast: jasmine.SpyObj<any>;

  beforeEach(() => {
    auth = jasmine.createSpyObj('AuthService', ['register']);
    navigation = jasmine.createSpyObj('NavigationService', ['goToLogin']);
    toast = jasmine.createSpyObj('ToastService', ['error', 'success']);
    component = new RegisterComponent(new FormBuilder(), navigation, auth, toast);
  });

  it('validates passwords, strength, visibility and submit state', () => {
    component.registerForm.patchValue({ password: 'abc', confirmPassword: 'xyz' });
    expect(component.isPasswordMismatch()).toBeFalse();
    component.submitted = true;
    expect(component.isPasswordMismatch()).toBeTrue();
    component.password?.setValue('Strong9!');
    component.confirmPassword?.setValue('Strong9!');
    component.onPasswordChange();
    expect(component.passwordStrength).toBe(4);
    expect(component.getPasswordStrengthClass()).toBe('strength-excellent');
    expect(component.getPasswordStrengthLabel()).toContain('Excellent');
    component.togglePassword();
    component.toggleConfirmPassword();
    expect(component.showPassword).toBeTrue();
    expect(component.showConfirmPassword).toBeTrue();
    component.terms?.setValue(true);
    expect(component.canSubmit()).toBeTrue();
    component.isLoading = true;
    expect(component.canSubmit()).toBeFalse();
  });

  it('reports password strength labels and invalid controls', () => {
    expect(component.getPasswordStrengthLabel()).toContain('minimum');
    for (const password of ['a', 'abcdefgh', 'Abcdefgh', 'Abcdefg1!']) {
      component.password?.setValue(password);
      component.onPasswordChange();
    }
    expect(component.getPasswordStrengthClass()).toBe('strength-excellent');
    component.submitted = true;
    expect(component.isInvalid('email')).toBeTrue();
    expect(component.isPasswordMismatch()).toBeFalse();
  });

  it('rejects invalid form and missing terms', () => {
    component.onSubmit();
    expect(toast.error).toHaveBeenCalled();
    component.registerForm.patchValue({
      firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', role: 'DOCTOR',
      password: 'Strong9!', confirmPassword: 'Strong9!', terms: false
    });
    component.terms?.setErrors(null);
    component.onSubmit();
    expect(toast.error).toHaveBeenCalledWith(jasmine.any(String), 'Conditions requises');
  });

  it('registers valid data and navigates on success', () => {
    component.registerForm.patchValue({
      firstName: ' Ada ', lastName: ' Lovelace ', email: 'ADA@EXAMPLE.COM', role: 'DOCTOR',
      institution: ' Clinic ', password: 'Strong9!', confirmPassword: 'Strong9!', terms: true
    });
    auth.register.and.returnValue(of({ message: 'Created' }));
    component.onSubmit();
    expect(auth.register).toHaveBeenCalledWith(jasmine.objectContaining({
      firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', establishment: 'Clinic'
    }));
    expect(toast.success).toHaveBeenCalledWith('Created', 'Inscription');
    expect(navigation.goToLogin).toHaveBeenCalled();
    expect(component.isLoading).toBeFalse();
  });

  it('handles registration errors and loading guard', () => {
    component.registerForm.patchValue({
      firstName: 'Ada', lastName: 'Lovelace', email: 'ada@example.com', role: 'DOCTOR',
      password: 'Strong9!', confirmPassword: 'Strong9!', terms: true
    });
    for (const status of [400, 409, 500, 418]) {
      auth.register.and.returnValue(throwError(() => ({ status, error: { message: 'failed' } })));
      component.onSubmit();
      expect(component.registrationError).toBeTrue();
      expect(component.isLoading).toBeFalse();
    }
    component.isLoading = true;
    auth.register.calls.reset();
    component.onSubmit();
    expect(auth.register).not.toHaveBeenCalled();
    component.goToLogin();
    expect(navigation.goToLogin).toHaveBeenCalled();
  });
});
