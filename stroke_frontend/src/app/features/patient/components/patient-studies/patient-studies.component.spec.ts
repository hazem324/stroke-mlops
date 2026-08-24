import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientStudiesComponent } from './patient-studies.component';

describe('PatientStudiesComponent', () => {
  let component: PatientStudiesComponent;
  let fixture: ComponentFixture<PatientStudiesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PatientStudiesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PatientStudiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
