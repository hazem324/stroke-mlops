import { Component, OnInit } from '@angular/core';
import { ToastService } from '../../../../services/toast.service';
import { NavigationService } from '../../../../services/navigation.service';
import { StudiesService } from '../../../../services/studies.service';
import { RecentAnalysis } from '../../../../models/studies/RecentAnalysis.model';

interface Analysis {
  studyId: number;
  patientId: number;
  sid: string;
  pid: string;
  name: string;
  date: string;
  result: 'lesion' | 'none' | 'pending';
  status: 'Completed' | 'Processing' | 'Failed' | 'Review' | 'Validated';
  model: string;
  modality: string;
}

@Component({
  selector: 'app-study-history',
  templateUrl: './study-history.component.html',
  styleUrl: './study-history.component.css'
})
export class StudyHistoryComponent implements OnInit {

  searchTerm = '';
  selectedResult = 'All results';
  selectedStatus = 'All statuses';
  selectedDate = '';

  analyses: Analysis[] = [];

  loading = false;

  constructor(
    private studiesService: StudiesService,
    private toast: ToastService,
    private navigationService: NavigationService
  ) {}

  ngOnInit(): void {
    this.loadAnalysesHistory();
  }

  private loadAnalysesHistory(): void {

  this.loading = true;

  this.studiesService.getAnalysesHistory().subscribe({

    next: (data) => {

      this.analyses = data.map(analysis => ({
        studyId: analysis.studyId,
        patientId: analysis.patientId,
        sid: analysis.studyCode,
        pid: analysis.patientCode,
        name: analysis.patientName,
        date: this.formatDate(analysis.studyDate),
        result: this.getResultType(analysis),
        status: this.getStatusType(analysis.status),
        model: 'stroke-dwi v1',
        modality: analysis.modality ?? '—'
      }));

      this.loading = false;

      console.log('Historique des analyses :', this.analyses);
    },

    error: (error) => {

      this.loading = false;

      console.error(
        'Erreur lors de la récupération de l’historique :',
        error
      );

      this.toast.error(
        'Impossible de récupérer l’historique des analyses.'
      );
    }

  });
}

  private formatDate(date: string): string {

    if (!date) {
      return '';
    }

    const parts = date.split('-');

    if (parts.length !== 3) {
      return date;
    }

    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }

  private getResultType(
    analysis: RecentAnalysis
  ): 'lesion' | 'none' | 'pending' {

    if (analysis.status !== 'COMPLETED') {
      return 'pending';
    }

    if (analysis.lesionDetected === true) {
      return 'lesion';
    }

    if (analysis.lesionDetected === false) {
      return 'none';
    }

    return 'pending';
  }

  private getStatusType(
    status: string | null
  ): Analysis['status'] {

    switch (status) {

      case 'COMPLETED':
        return 'Completed';

      case 'PROCESSING':
        return 'Processing';

      case 'FAILED':
        return 'Failed';

      case 'REVIEW':
        return 'Review';

      case 'VALIDATED':
        return 'Validated';

      default:
        return 'Processing';
    }
  }

  get filteredAnalyses(): Analysis[] {

    return this.analyses.filter(analysis => {

      const search = this.searchTerm
        .toLowerCase()
        .trim();

      const matchesSearch =
        !search ||
        analysis.sid.toLowerCase().includes(search) ||
        analysis.pid.toLowerCase().includes(search) ||
        analysis.name.toLowerCase().includes(search);

      const matchesResult =
        this.selectedResult === 'All results' ||
        (
          this.selectedResult === 'Lesion detected' &&
          analysis.result === 'lesion'
        ) ||
        (
          this.selectedResult === 'No lesion' &&
          analysis.result === 'none'
        );

      const matchesStatus =
        this.selectedStatus === 'All statuses' ||
        (
          this.selectedStatus === 'Completed' &&
          analysis.status === 'Completed'
        ) ||
        (
          this.selectedStatus === 'Processing' &&
          analysis.status === 'Processing'
        ) ||
        (
          this.selectedStatus === 'Failed' &&
          analysis.status === 'Failed'
        ) ||
        (
          this.selectedStatus === 'Waiting for review' &&
          analysis.status === 'Review'
        ) ||
        (
          this.selectedStatus === 'Validated' &&
          analysis.status === 'Validated'
        );

      const matchesDate =
        !this.selectedDate ||
        this.formatDateForInput(analysis.date) === this.selectedDate;

      return (
        matchesSearch &&
        matchesResult &&
        matchesStatus &&
        matchesDate
      );
    });
  }

  get resultCount(): number {
    return this.analyses.length;
  }

  get filteredCount(): number {
    return this.filteredAnalyses.length;
  }

  getResultLabel(
    result: Analysis['result']
  ): string {

    switch (result) {

      case 'lesion':
        return 'Lesion detected';

      case 'none':
        return 'No lesion';

      default:
        return '—';
    }
  }

  getStatusLabel(
    status: Analysis['status']
  ): string {

    switch (status) {

      case 'Completed':
        return 'Completed';

      case 'Processing':
        return 'Processing';

      case 'Failed':
        return 'Failed';

      case 'Review':
        return 'Waiting for review';

      case 'Validated':
        return 'Validated';

      default:
        return status;
    }
  }

  goToAnalysis(analysis: Analysis): void {

    console.log('View analysis:', analysis);

    this.navigationService.goToAnalysisDetail(
      analysis.studyId
    );
  }

  previousPage(): void {
    console.log('Previous page');
  }

  nextPage(): void {
    console.log('Next page');
  }

  private formatDateForInput(date: string): string {

    const parts = date.split('/');

    if (parts.length !== 3) {
      return '';
    }

    return `${parts[2]}-${parts[1]}-${parts[0]}`;
  }
}