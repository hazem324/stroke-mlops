import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { StudyAnalysisComponent } from './components/study-analysis/study-analysis.component';
import { AnalysisResultComponent } from './components/analysis-result/analysis-result.component';

const routes: Routes = [

  {
    path: '',
    redirectTo: 'new-analysis',
    pathMatch: 'full'
  },

  {
    path: 'new-analysis',
    component: StudyAnalysisComponent
  },

  {
    path: 'detail-analyse/:studyId',
    component: AnalysisResultComponent
  }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StudyRoutingModule {}