import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { StudyRoutingModule } from './study-routing.module';
import { StudyAnalysisComponent } from './components/study-analysis/study-analysis.component';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StudyRoutingModule,
    StudyAnalysisComponent
  ]
})
export class StudyModule { }
