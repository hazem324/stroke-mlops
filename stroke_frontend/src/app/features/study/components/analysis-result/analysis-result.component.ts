import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Location } from '@angular/common';

import { ActivatedRoute } from '@angular/router';
import { Niivue } from '@niivue/niivue';

import { StudyDetail } from '../../../../models/studies/study-detail.model';
import { StudiesService } from '../../../../services/studies.service';
import { ToastService } from '../../../../services/toast.service';
import { environment } from '../../../../../environments/environment';

type DisplayMode = 'dwi' | 'prediction' | 'overlay' | 'dwi_prediction';

@Component({
  selector: 'app-analysis-result',
  templateUrl: './analysis-result.component.html',
  styleUrl: './analysis-result.component.css'
})
export class AnalysisResultComponent implements OnInit {

  study: StudyDetail | null = null;

  loading = false;
  error = '';

  displayMode: DisplayMode = 'dwi_prediction'; // valeur par défaut

  private niivue: Niivue | null = null;
  private niivueReady = false;
  private canvasEl: HTMLCanvasElement | null = null;

  @ViewChild('niivueCanvas')
  set niivueCanvas(ref: ElementRef<HTMLCanvasElement> | undefined) {
    if (ref) {
      this.canvasEl = ref.nativeElement;
      this.initNiivue();
    }
  }

  constructor(
    private route: ActivatedRoute,
    private studiesService: StudiesService,
    private toast: ToastService,
    private location: Location
  ) {}

  ngOnInit(): void {
    const studyIdParam = this.route.snapshot.paramMap.get('studyId');

    if (!studyIdParam) {
      this.error = 'Study ID introuvable dans l’URL';
      return;
    }

    const studyId = Number(studyIdParam);

    if (isNaN(studyId) || studyId <= 0) {
      this.error = 'Study ID invalide';
      return;
    }

    this.loadStudy(studyId);
  }

  private async initNiivue(): Promise<void> {
    if (this.niivueReady || !this.canvasEl) {
      return;
    }

    try {
      this.niivue = new Niivue({
        isColorbar: true,
        show3Dcrosshair: true,
        isResizeCanvas: true
      });

      await this.niivue.attachToCanvas(this.canvasEl);
      this.niivueReady = true;

      if (this.study) {
        await this.loadVolumesForMode(this.displayMode);
      }
    } catch (err) {
      console.error('Error attaching Niivue to canvas:', err);
    }
  }

  private loadStudy(studyId: number): void {
    this.loading = true;
    this.error = '';

    this.studiesService
      .getDetaileAnalyse(studyId)
      .subscribe({
        next: async (response: StudyDetail) => {
          console.log('Study detail:', response);

          this.study = response;
          this.loading = false;

          if (this.niivueReady) {
            await this.loadVolumesForMode(this.displayMode);
          }
        },
        error: (error) => {
          console.error('Error loading study detail:', error);
          this.error = 'Impossible de récupérer les résultats de l’analyse.';
          this.loading = false;
        }
      });
  }

  /** Appelé quand l'utilisateur change le <select>. */
  async onDisplayModeChange(mode: DisplayMode): Promise<void> {
    this.displayMode = mode;
    await this.loadVolumesForMode(mode);
  }

  /**
   * Construit la liste de volumes à charger dans Niivue selon le mode
   * choisi, puis les charge.
   */
  private async loadVolumesForMode(mode: DisplayMode): Promise<void> {
  if (!this.niivue || !this.niivueReady || !this.study) {
    return;
  }

  const prediction = this.study.prediction;

  const dwiUrl = this.buildDwiUrl();   // ← await ajouté ici
  const predictionUrl = prediction?.predictionFile
    ? `${environment.storageBaseUrl}${prediction.predictionFile}`
    : null;
  const overlayUrl = prediction?.overlayFile
    ? `${environment.storageBaseUrl}${prediction.overlayFile}`
    : null;

  const volumes: { url: string; colormap?: string; opacity?: number }[] = [];

  try {
    switch (mode) {

      case 'dwi':
        if (!dwiUrl) {
          this.toast.error('Fichier DWI original introuvable.', 'Erreur de visualisation');
          return;
        }
        volumes.push({ url: dwiUrl, colormap: 'gray', opacity: 1 });
        break;

      case 'prediction':
        if (!predictionUrl) {
          this.toast.error('Fichier de prédiction introuvable.', 'Erreur de visualisation');
          return;
        }
        volumes.push({ url: predictionUrl, colormap: 'gray', opacity: 1 });
        break;

      case 'overlay':
        if (!overlayUrl) {
          this.toast.error('Fichier de superposition introuvable.', 'Erreur de visualisation');
          return;
        }
        volumes.push({ url: overlayUrl, colormap: 'gray', opacity: 1 });
        break;

      case 'dwi_prediction':
        if (!dwiUrl || !predictionUrl) {
          this.toast.error('Fichiers DWI ou prédiction introuvables.', 'Erreur de visualisation');
          return;
        }
        volumes.push({ url: dwiUrl, colormap: 'gray', opacity: 1 });
        volumes.push({ url: predictionUrl, colormap: 'red', opacity: 0.6 });
        break;
    }

    await this.niivue.loadVolumes(volumes);
    this.niivue.setSliceType(this.niivue.sliceTypeMultiplanar);

    console.log(`Volumes chargés pour le mode "${mode}"`);
    this.toast.success('La visualisation a été mise à jour.', 'Visualisation prête');

  } catch (error) {
    console.error('Error loading NIfTI volumes:', error);
    this.toast.error(
      'Impossible de charger le(s) fichier(s) NIfTI.',
      'Erreur de visualisation'
    );
  }
}


  formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleString('fr-FR');
  }

  goBack(): void {
    this.location.back();
  }


private buildDwiUrl(): string | null {

  if (!this.study || !this.study.dwiFileName) {
    return null;
  }

  return `${environment.storageBaseUrl}patients/${this.study.patientId}/studies/${this.study.studyCode}/dwi.nii.gz`;
}

getPreviewUrl(): string {
  if (!this.study) {
    return 'assets/images/no-preview.png';
  }

  return `${environment.storageBaseUrl}patients/${this.study.patientId}/studies/${this.study.studyCode}/analysis/preview.png`;
}

}