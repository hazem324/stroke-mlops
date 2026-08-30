import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { ToastService } from '../../services/toast.service';
import { DashboardStatistics } from '../../models/dashboard/dashboard-statistics.model';
import { RecentAnalysis } from '../../models/dashboard/recent-analysis.model';
import { NavigationService } from '../../services/navigation.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  statistics!: DashboardStatistics;

  recentAnalyses: RecentAnalysis[] = [];

  loadingStatistics = false;
  loadingRecentAnalyses = false;

  constructor(
    private dashboardService: DashboardService,
    private toast: ToastService, 
    private navigationService: NavigationService
  ) {}

  ngOnInit(): void {
    this.loadStatistics();
    this.loadRecentAnalyses();
  }

  private loadStatistics(): void {

    this.loadingStatistics = true;

    this.dashboardService.getStatistics().subscribe({

      next: (data: DashboardStatistics) => {

        console.log('Statistiques reçues :', data);

        this.statistics = data;
        this.loadingStatistics = false;
      },

      error: (error) => {

        this.loadingStatistics = false;

        console.error(
          'Erreur lors de la récupération des statistiques :',
          error
        );

        this.toast.error(
          'Impossible de récupérer les statistiques du tableau de bord.'
        );
      }

    });
  }

  private loadRecentAnalyses(): void {

    this.loadingRecentAnalyses = true;

    this.dashboardService.getRecentAnalyses().subscribe({

      next: (data: RecentAnalysis[]) => {

        console.log('Analyses récentes reçues :', data);

        this.recentAnalyses = data;
        this.loadingRecentAnalyses = false;
      },

      error: (error) => {

        this.loadingRecentAnalyses = false;

        console.error(
          'Erreur lors de la récupération des analyses récentes :',
          error
        );

        this.toast.error(
          'Impossible de récupérer les analyses récentes.'
        );
      }

    });
  }

  getInitials(name: string | null | undefined): string {

    if (!name) {
      return '';
    }

    return name
      .trim()
      .split(/\s+/)
      .map(part => part.charAt(0).toUpperCase())
      .slice(0, 2)
      .join('');
  }

  getResultClass(analysis: RecentAnalysis): string {

    const status = analysis.status?.toUpperCase();

    if (status !== 'COMPLETED') {
      return 'pending';
    }

    return analysis.lesionDetected
      ? 'detected'
      : 'no-lesion';
  }

  getResultLabel(analysis: RecentAnalysis): string {

    const status = analysis.status?.toUpperCase();

    if (status !== 'COMPLETED') {
      return 'En attente';
    }

    return analysis.lesionDetected
      ? 'Lésion détectée'
      : 'Aucune lésion';
  }

  formatStudyDate(date: string | null | undefined): string {

    if (!date) {
      return '-';
    }

    const [year, month, day] = date.split('-').map(Number);

    if (!year || !month || !day) {
      return date;
    }

    const months = [
      'janvier',
      'février',
      'mars',
      'avril',
      'mai',
      'juin',
      'juillet',
      'août',
      'septembre',
      'octobre',
      'novembre',
      'décembre'
    ];

    return `${day} ${months[month - 1]} ${year}`;
  }

  goToAnalysisDetail(id : number) : void {
    return this.navigationService.goToAnalysisDetail(id);
  }

  goToNewAnalyse() : void {
    return this.navigationService.goToNewAnalysis();
  }

  goToHistory() : void {
    return this.navigationService.goToAnalysisHistory();
  }
}