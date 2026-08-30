import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { CurrentUser } from '../../../../models/user/current-user.model';
import { UserService } from '../../../../services/user.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {

  pageTitle = 'Tableau de bord';
  pageSubtitle = 'Vue d’ensemble de votre activité';

  currentUser: CurrentUser | null = null;
  loadingUser = false;

  constructor(
    private router: Router,
    private userService: UserService
  ) {}

  ngOnInit(): void {

    this.updatePageTitle(this.router.url);

    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd)
      )
      .subscribe(event => {

        const navigation = event as NavigationEnd;

        this.updatePageTitle(navigation.urlAfterRedirects);

      });

    this.loadCurrentUser();
  }

  private loadCurrentUser(): void {

    this.loadingUser = true;

    this.userService.getCurrentUser().subscribe({

      next: (user: CurrentUser) => {

        this.currentUser = user;
        this.loadingUser = false;

        console.log('Utilisateur connecté :', this.currentUser);
      },

      error: (error) => {

        this.loadingUser = false;

        console.error(
          'Erreur lors de la récupération de l’utilisateur connecté :',
          error
        );
      }

    });
  }

  private updatePageTitle(url: string): void {

    // Dashboard
    if (url.includes('/dashboard/home')) {

      this.pageTitle = 'Tableau de bord';
      this.pageSubtitle = 'Vue d’ensemble de votre activité';

    }

    // Nouvelle analyse
    else if (url.includes('/analysis/new-analysis')) {

      this.pageTitle = 'Nouvelle analyse IRM';
      this.pageSubtitle = 'Lancer une nouvelle analyse par IA';

    }

    // Historique des analyses
    else if (url.includes('/analysis/analysis-history')) {

      this.pageTitle = 'Historique des analyses';
      this.pageSubtitle = 'Historique de toutes les analyses';

    }

    // Analyses
    else if (url.includes('/analysis')) {

      this.pageTitle = 'Analyses';
      this.pageSubtitle = 'Gérer les analyses IRM';

    }

    // Patients
    else if (url.includes('/patient')) {

      this.pageTitle = 'Patients';
      this.pageSubtitle = 'Gérer les dossiers patients';

    }

    // Par défaut
    else {

      this.pageTitle = 'Tableau de bord';
      this.pageSubtitle = 'Vue d’ensemble de votre activité';

    }
  }

  getUserFullName(): string {

    if (!this.currentUser) {
      return 'Chargement...';
    }

    return `Dr. ${this.currentUser.firstName} ${this.currentUser.lastName}`;
  }

  getUserInitials(): string {

    if (!this.currentUser) {
      return '...';
    }

    const firstNameInitial =
      this.currentUser.firstName?.charAt(0).toUpperCase() || '';

    const lastNameInitial =
      this.currentUser.lastName?.charAt(0).toUpperCase() || '';

    return firstNameInitial + lastNameInitial;
  }

  getUserRole(): string {

    if (!this.currentUser?.role) {
      return '';
    }

    switch (this.currentUser.role) {

      case 'DOCTOR':
        return 'Doctor';

      case 'ADMIN':
        return 'Administrateur';

      default:
        return this.currentUser.role;
    }
  }
}