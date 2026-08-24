import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent
  },

  {
    path: 'dashboard',
    loadChildren: () =>
      import('./layout/front-office/front-office.module')
        .then(m => m.FrontOfficeModule)
  },

  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.module')
        .then(m => m.AuthModule)
  }
];