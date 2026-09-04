import { HttpRequest, HttpResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  it('should add Authorization header when a token exists', () => {
    const authService = jasmine.createSpyObj<AuthService>('AuthService', ['getToken']);
    authService.getToken.and.returnValue('abc123');

    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authService }]
    });

    const req = new HttpRequest('GET', '/api/test');
    const next = jasmine.createSpy('next').and.callFake((request) => of(new HttpResponse({ status: 200, body: 'ok' })));

    const result = TestBed.runInInjectionContext(() => authInterceptor(req, next as any));

    result.subscribe(() => {
      const outgoing = next.calls.mostRecent().args[0];
      expect(outgoing.headers.get('Authorization')).toBe('Bearer abc123');
    });
  });
});
