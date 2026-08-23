import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PatientRoutingModule } from './patient-routing.module';
import { PatientListComponent } from './pages/patient-list/patient-list.component';
import { PatientDetailsComponent } from './pages/patient-details/patient-details.component';
import { PatientCardComponent } from './components/patient-card/patient-card.component';
import { AddPatientModalComponent } from './components/add-patient-modal/add-patient-modal.component';

@NgModule({
  declarations: [
    PatientListComponent,
    PatientDetailsComponent,
    PatientCardComponent,
    AddPatientModalComponent
  ],

  imports: [
    CommonModule,
    PatientRoutingModule
  ]
})
export class PatientModule {}