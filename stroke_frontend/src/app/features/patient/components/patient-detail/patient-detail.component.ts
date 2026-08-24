import { Component, Input } from '@angular/core';

import { Patient } from '../../../../models/patient/patient.model';

@Component({
  selector: 'app-patient-detail',
  templateUrl: './patient-detail.component.html',
  styleUrl: './patient-detail.component.css'
})
export class PatientDetailComponent {

  @Input()
  patient!: Patient;

}