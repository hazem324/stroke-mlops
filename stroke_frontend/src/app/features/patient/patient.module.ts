import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { PatientRoutingModule } from './patient-routing.module';
import { PatientListComponent } from './pages/patient-list/patient-list.component';
import { PatientDetailsComponent } from './pages/patient-details/patient-details.component';
import { PatientCardComponent } from './components/patient-card/patient-card.component';
import { AddPatientModalComponent } from './components/add-patient-modal/add-patient-modal.component';
import { PatientDetailComponent } from './components/patient-detail/patient-detail.component';
import { PatientStudiesComponent } from './components/patient-studies/patient-studies.component';
import { PatientDetailPageComponent } from './pages/patient-detail-page/patient-detail-page.component';

@NgModule({
  declarations: [
    PatientListComponent,
    PatientDetailsComponent,
    PatientCardComponent,
    AddPatientModalComponent,
    PatientDetailComponent,
    PatientStudiesComponent,
    PatientDetailPageComponent
  ],

  imports: [
    CommonModule,
    PatientRoutingModule,
    ReactiveFormsModule
  ]
})
export class PatientModule {}