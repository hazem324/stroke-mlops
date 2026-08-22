import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

import { environment } from '../../environments/environment';

import { LoginRequest } from '../models/auth/login-request.model';
import { LoginResponse } from '../models/auth/login-response.model';
import { RegisterRequest } from '../models/auth/register-request.model';
import { RegisterResponse } from '../models/auth/register-response.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly baseUrl = environment.apiBaseUrl + '/auth';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}


  // REGISTER
  register(
    request: RegisterRequest
  ): Observable<RegisterResponse> {

    return this.http.post<RegisterResponse>(
      `${this.baseUrl}/register`,
      request
    );
  }


  // LOGIN
  login(
    request: LoginRequest
  ): Observable<LoginResponse> {

    return this.http
      .post<LoginResponse>(
        `${this.baseUrl}/login`,
        request
      )
      .pipe(

        tap(response => {

          // Save JWT after successful login
          this.saveToken(response.token);

        })

      );
  }


  // TOKEN
  saveToken(token: string): void {

    localStorage.setItem('token', token);
  }


  getToken(): string | null {

    const token = localStorage.getItem('token');

    if (token && this.isTokenExpired(token)) {

      this.logout();

      return null;
    }

    return token;
  }


  // TOKEN EXPIRATION
  isTokenExpired(token: string): boolean {

    try {

      const decoded: any = jwtDecode(token);

      // No expiration claim
      if (!decoded.exp) {
        return true;
      }

      return decoded.exp * 1000 < Date.now();

    } catch (e) {

      // Invalid token = expired
      return true;
    }
  }

  // AUTHENTICATION
  isAuthenticated(): boolean {

    const token = this.getToken();

    if (!token) {
      return false;
    }

    if (this.isTokenExpired(token)) {

      this.logout();

      return false;
    }

    return true;
  }

  // ROLE
  getRoleFromToken(): string | null {

    const token = this.getToken();

    if (!token) {
      return null;
    }

    try {

      const decoded: any = jwtDecode(token);
      return decoded?.role || null;

    } catch (e) {

      return null;
    }
  }


  // LOGOUT
  logout(): void {

    localStorage.removeItem('token');
    localStorage.removeItem('user');

    if (this.router.url !== '/auth/login') {

      this.router.navigate(['/auth/login']);
    }
  }
}