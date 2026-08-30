import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LayoutComponent } from './pages/layout/layout.component';
import { DashboardComponent } from '../../pages/dashboard/dashboard.component';

const routes: Routes = [

  {
    path: '',
    component: LayoutComponent,

    children: [
      {
        path: 'home',
        component: DashboardComponent
      },

      {
        path: 'patient',

        loadChildren: () =>
          import('../../features/patient/patient.module')
            .then(m => m.PatientModule)
      }, 

    {
      path: 'analysis', 
      loadChildren: () =>
        import('../../features/study/study.module')
      .then(m => m.StudyModule)
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