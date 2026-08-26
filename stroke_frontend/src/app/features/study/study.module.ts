import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { StudyRoutingModule } from './study-routing.module';
import { StudyAnalysisComponent } from './components/study-analysis/study-analysis.component';


@NgModule({
  declarations: [
    StudyAnalysisComponent
  ],
  imports: [
    FormsModule,
    CommonModule,
    StudyRoutingModule,
  ]
})
export class StudyModule { }
