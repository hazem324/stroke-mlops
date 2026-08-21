import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { APP_ROUTES } from '../core/config/app-routes';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {

  constructor(
    private router: Router
  ) {}


  /* Auth routes */
  goToLogin(): void {
    this.router.navigateByUrl(
      APP_ROUTES.auth.login
    );
  }

  goToRegister(): void {
    this.router.navigateByUrl(
      APP_ROUTES.auth.register
    );
  }

}
