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


  /* Dashboard routes */

  goToHomeDash(): void {
    this.router.navigateByUrl(
      APP_ROUTES.dash.home
    );
  }

  goToPatient(): void {
    this.router.navigateByUrl(
      APP_ROUTES.dash.patient
    );
  }


  /* Analysis routes */

  goToAnalysis(): void {
   // this.router.navigateByUrl(
    //  APP_ROUTES.analysis.
    // );
  }

  goToNewAnalysis(): void {
    this.router.navigateByUrl(
      APP_ROUTES.analysis.newAnalysis
    );
  }

  goToAnalysisDetail(studyId: number): void {
    this.router.navigate([
      APP_ROUTES.analysis.detailAnalysis,
      studyId
    ]);
  }

  goToAnalysisHistory(): void {
    this.router.navigateByUrl(
      APP_ROUTES.dash.history
    );
  }

}