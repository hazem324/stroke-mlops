import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { PatientService } from '../../../../services/patient.service';
import { ToastService } from '../../../../services/toast.service';
import { EditPatientModalComponent } from './edit-patient-modal.component';

describe('EditPatientModalComponent', () => {
  let component: EditPatientModalComponent;
  let fixture: ComponentFixture<EditPatientModalComponent>;
  let patientService: jasmine.SpyObj<PatientService>;

  beforeEach(async () => {
    patientService = jasmine.createSpyObj<PatientService>('PatientService', ['getPatientById']);
    patientService.getPatientById.and.returnValue(of({
      patient: {
        id: 42,
        patientCode: 'P-0001',
        firstName: 'Alice',
        lastName: 'Example',
        dateOfBirth: '1990-01-01',
        sex: 'FEMALE',
        age: 35,
        weight: 68,
        phoneNumber: '123456789'
      }
    }));

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [EditPatientModalComponent],
      providers: [
        { provide: PatientService, useValue: patientService },
        { provide: ToastService, useValue: jasmine.createSpyObj('ToastService', ['error', 'success']) }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EditPatientModalComponent);
    component = fixture.componentInstance;
    component.patientId = 42;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load the selected patient when patientId is set', () => {
    component.ngOnChanges({ patientId: { currentValue: 42, previousValue: undefined, firstChange: true, isFirstChange: () => true } });
    expect(patientService.getPatientById).toHaveBeenCalledWith(42);
    expect(component.patient?.firstName).toBe('Alice');
  });
});
