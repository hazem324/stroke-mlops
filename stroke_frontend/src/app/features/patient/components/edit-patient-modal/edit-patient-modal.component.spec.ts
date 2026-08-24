import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditPatientModalComponent } from './edit-patient-modal.component';

describe('EditPatientModalComponent', () => {
  let component: EditPatientModalComponent;
  let fixture: ComponentFixture<EditPatientModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EditPatientModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditPatientModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
