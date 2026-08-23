import { Component } from '@angular/core';

@Component({
  selector: 'app-patient-list',
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.css'
})
export class PatientListComponent {

  showAddPatientModal = false;

   patients = [
    {
      id: 'P001',
      name: 'John Doe',
      dob: '14/03/1968',
      studies: 3,
      lastAnalysis: '18/08/2026',
      status: 'lesion'
    },
    {
      id: 'P002',
      name: 'Sonia Ben Ali',
      dob: '22/11/1975',
      studies: 1,
      lastAnalysis: '18/08/2026',
      status: 'none'
    },
    {
      id: 'P014',
      name: 'Karim Haddad',
      dob: '05/06/1959',
      studies: 2,
      lastAnalysis: '17/08/2026',
      status: 'processing'
    },
    {
      id: 'P027',
      name: 'Emna Gharbi',
      dob: '30/01/1982',
      studies: 4,
      lastAnalysis: '17/08/2026',
      status: 'lesion'
    },
    {
      id: 'P031',
      name: 'Youssef Mestiri',
      dob: '19/09/1990',
      studies: 1,
      lastAnalysis: '16/08/2026',
      status: 'review'
    }
  ];

  openAddPatientModal(): void {
  this.showAddPatientModal = true;
}

closeAddPatientModal(): void {
  this.showAddPatientModal = false;
}

}
