import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StudyAnalysisComponent } from './components/study-analysis/study-analysis.component';

const routes: Routes = [

  {
    path: '',
    component: StudyAnalysisComponent
  }
  
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StudyRoutingModule { }
