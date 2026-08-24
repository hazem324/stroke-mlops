import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PatientListComponent } from './pages/patient-list/patient-list.component';
import { PatientDetailPageComponent } from './pages/patient-detail-page/patient-detail-page.component';

const routes: Routes = [
   {
    path: '',
    component: PatientListComponent
  },

 {
  path: ':id',
  component: PatientDetailPageComponent
}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PatientRoutingModule { }
