import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should login and persist JWT', () => {
    service.login({ email: 'doctor@example.com', password: 'Password1' }).subscribe((response) => {
      expect(response.message).toBe('Connexion réussie');
      expect(response.token).toBe('jwt-token');
      expect(localStorage.getItem('token')).toBe('jwt-token');
    });

    const req = httpMock.expectOne('http://stroke.local/stroke_ml/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({ message: 'Connexion réussie', token: 'jwt-token', expiresIn: 3600 });
  });

  it('should detect expired tokens as invalid', () => {
    const expiredToken = 'invalid.token';
    localStorage.setItem('token', expiredToken);

    expect(service.getToken()).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('should return role from JWT payload when token is valid', () => {
    const header = btoa(JSON.stringify({ alg: 'HS384', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({ role: 'DOCTOR', exp: Math.floor((Date.now() + 60000) / 1000) }));
    const signature = btoa('signature');
    const token = `${header}.${payload}.${signature}`;
    localStorage.setItem('token', token);

    expect(service.getRoleFromToken()).toBe('DOCTOR');
  });
});
