import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  
 pageTitle = 'Tableau de bord';
  pageSubtitle = 'Vue d’ensemble de votre activité';

  constructor(private router: Router) {}

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
  }

  private updatePageTitle(url: string): void {

    if (url.includes('/dashboard/home')) {

      this.pageTitle = 'Tableau de bord';
      this.pageSubtitle = 'Vue d’ensemble de votre activité';

    } else if (url.includes('/patient')) {

      this.pageTitle = 'Patients';
      this.pageSubtitle = 'Gérer les dossiers patients';

    } else if (url.includes('/analysis')) {

      this.pageTitle = 'Analyses';
      this.pageSubtitle = 'Gérer les analyses IRM';

    } else {

      this.pageTitle = 'Tableau de bord';
      this.pageSubtitle = 'Vue d’ensemble de votre activité';
    }
  }
}