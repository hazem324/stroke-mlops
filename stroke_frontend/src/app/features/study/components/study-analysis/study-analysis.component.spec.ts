import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { StudyAnalysisComponent } from './study-analysis.component';

describe('StudyAnalysisComponent', () => {
  let component: StudyAnalysisComponent;
  let patients: jasmine.SpyObj<any>;
  let studies: jasmine.SpyObj<any>;
  let toast: jasmine.SpyObj<any>;

  beforeEach(() => {
    patients = jasmine.createSpyObj('PatientService', ['getMyPatients']);
    studies = jasmine.createSpyObj('StudiesService', ['analyzeStudy']);
    toast = jasmine.createSpyObj('ToastService', ['error', 'info', 'success']);
    patients.getMyPatients.and.returnValue(of({ patients: [{ id: 1, firstName: 'A' }] }));
    component = new StudyAnalysisComponent(patients, toast, studies);
  });

  it('loads patients and selects the requested patient', () => {
    component.ngOnInit();
    expect(component.patients.length).toBe(1);
    component.selectedPatientId = 1;
    component.onPatientChange();
    expect(component.selectedPatient?.id).toBe(1);
    component.selectedPatientId = null;
    component.onPatientChange();
    expect(component.selectedPatient).toBeNull();
  });

  it('reports patient loading errors', () => {
    for (const status of [403, 404, 500, 418]) {
      patients.getMyPatients.and.returnValue(throwError(() => new HttpErrorResponse({ status })));
      component.ngOnInit();
      expect(toast.error).toHaveBeenCalled();
    }
  });

  it('validates files, formats sizes and manages drag/drop state', () => {
    component.onDragEnter(new DragEvent('dragenter'));
    expect(component.isDragging).toBeTrue();
    component.onDragLeave(new DragEvent('dragleave'));
    expect(component.isDragging).toBeFalse();
    component.onDrop(new DragEvent('drop'));
    component.onFileSelected({ target: { files: [new File(['bad'], 'scan.txt')] } } as any);
    expect(component.selectedFile).toBeNull();
    const file = new File(['nii'], 'scan.nii.gz');
    component.onFileSelected({ target: { files: [file] } } as any);
    expect(component.selectedFile).toBe(file);
    expect(component.formatFileSize(0)).toBe('0 octet');
    expect(component.formatFileSize(1024)).toBe('1.0 Ko');
    component.removeFile();
    expect(component.selectedFile).toBeNull();
  });

  it('starts analysis successfully and handles every response error', () => {
    const file = new File(['nii'], 'scan.nii.gz');
    component.selectedPatientId = 1;
    component.selectedFile = file;
    studies.analyzeStudy.and.returnValue(of({ id: 1 }));
    component.startAnalysis();
    expect(toast.success).toHaveBeenCalled();
    expect(component.selectedFile).toBeNull();

    for (const status of [400, 403, 404, 422, 500, 418]) {
      component.selectedPatientId = 1;
      component.selectedFile = file;
      studies.analyzeStudy.and.returnValue(throwError(() => new HttpErrorResponse({ status })));
      component.startAnalysis();
      expect(toast.error).toHaveBeenCalled();
    }
    component.isLoading = true;
    component.startAnalysis();
    component.cancel();
    expect(component.isLoading).toBeTrue();
  });
});
