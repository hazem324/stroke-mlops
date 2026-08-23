import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';

export const routes: Routes = [

    {
        path: '',
        component: HomeComponent
    },

    {path: 'auth',
        loadChildren: ()=>
            import('./features/auth/auth.module')
            .then(m => m.AuthModule)
    }, 
    {
        path: 'patient',
        loadChildren: () => 
            import('./features/patient/patient.module')
        .then(m =>m.PatientModule)
    }
];
