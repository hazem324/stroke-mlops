import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';

import { PatientService } from './patient.service';
import { StudiesService } from './studies.service';
import { DashboardService } from './dashboard.service';
import { UserService } from './user.service';
import { NavigationService } from './navigation.service';
import { ToastService } from './toast.service';
import { ToastrService } from 'ngx-toastr';

describe('HTTP services', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        PatientService,
        StudiesService,
        DashboardService,
        UserService,
        NavigationService,
        ToastService,
        { provide: ToastrService, useValue: jasmine.createSpyObj('ToastrService', ['success', 'error', 'info', 'warning']) }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('calls all patient endpoints with expected methods and payloads', () => {
    const service = TestBed.inject(PatientService);
    service.getMyPatients().subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/patient/by-docktor').request.method).toBe('GET');

    service.getPatientById(7).subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/patient/7').request.method).toBe('GET');

    const patient = { firstName: 'A', lastName: 'B' } as any;
    service.createPatient(patient).subscribe();
    const create = http.expectOne('http://stroke.local/stroke_ml/api/patient');
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toBe(patient);

    service.updatePatient(7, patient).subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/patient/7').request.method).toBe('PUT');
  });

  it('calls study endpoints and builds multipart analysis requests', () => {
    const service = TestBed.inject(StudiesService);
    const file = new File(['volume'], 'scan.nii.gz', { type: 'application/gzip' });
    service.analyzeStudy(4, file).subscribe();
    const analyze = http.expectOne('http://stroke.local/stroke_ml/api/studies/4/analyze?modality=DWI');
    expect(analyze.request.method).toBe('POST');
    expect(analyze.request.body.get('file')).toBe(file);

    service.getStudiesByPatient(4).subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/studies/patients/4').request.method).toBe('GET');
    service.getStudy(8).subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/studies/8').request.method).toBe('GET');
    service.getDetaileAnalyse(8).subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/studies/study-detail/8').request.method).toBe('GET');
    service.getAnalysesHistory().subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/studies/analysis-history').request.method).toBe('GET');
  });

  it('calls dashboard and current-user endpoints', () => {
    const dashboard = TestBed.inject(DashboardService);
    dashboard.getStatistics().subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/dashboard/statistics').request.method).toBe('GET');
    dashboard.getRecentAnalyses().subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/dashboard/recent-analyses').request.method).toBe('GET');

    TestBed.inject(UserService).getCurrentUser().subscribe();
    expect(http.expectOne('http://stroke.local/stroke_ml/api/user/me').request.method).toBe('GET');
  });

  it('navigates to each supported application destination', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true));
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    const service = TestBed.inject(NavigationService);
    service.goToLogin();
    service.goToRegister();
    service.goToHomeDash();
    service.goToPatient();
    service.goToNewAnalysis();
    service.goToAnalysisDetail(12);
    service.goToAnalysisHistory();
    expect(router.navigateByUrl).toHaveBeenCalledTimes(6);
    expect(router.navigate).toHaveBeenCalledWith(jasmine.any(Array));
  });

  it('delegates toast messages and supports default titles', () => {
    const toastr = TestBed.inject(ToastrService) as jasmine.SpyObj<ToastrService>;
    const service = TestBed.inject(ToastService);
    service.success('ok');
    service.error('bad', 'Oops');
    service.info('info');
    service.warning('warn');
    expect(toastr.success).toHaveBeenCalledWith('ok', 'Succès');
    expect(toastr.error).toHaveBeenCalledWith('bad', 'Oops');
    expect(toastr.info).toHaveBeenCalledWith('info', 'Information');
    expect(toastr.warning).toHaveBeenCalledWith('warn', 'Attention');
  });
});
