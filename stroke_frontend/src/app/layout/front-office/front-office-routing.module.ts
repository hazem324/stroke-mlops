import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LayoutComponent } from './pages/layout/layout.component';

const routes: Routes = [

  {
    path: '',
    component: LayoutComponent,

    children: [

      {
        path: 'patient',

        loadChildren: () =>
          import('../../features/patient/patient.module')
            .then(m => m.PatientModule)
      }

    ]
  }

];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],

  exports: [
    RouterModule
  ]
})
export class FrontOfficeRoutingModule { }