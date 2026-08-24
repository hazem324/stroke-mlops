import { Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-patient-studies',
  templateUrl: './patient-studies.component.html',
  styleUrl: './patient-studies.component.css'
})
export class PatientStudiesComponent implements OnInit {

  @Input()
  patientId!: number;

  ngOnInit(): void {

    console.log(
      'Patient ID:',
      this.patientId
    );

  }

}