import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


import { DashboardStatistics } from '../models/dashboard/dashboard-statistics.model';
import { RecentAnalysis } from '../models/dashboard/recent-analysis.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private http: HttpClient) { }

    private readonly baseUrl = environment.apiBaseUrl + '/api/dashboard';

    
  getStatistics(): Observable<DashboardStatistics> {
    return this.http.get<DashboardStatistics>(
      `${this.baseUrl}/statistics`
    );
  }

  getRecentAnalyses(): Observable<RecentAnalysis[]> {
    return this.http.get<RecentAnalysis[]>(
      `${this.baseUrl}/recent-analyses`
    );
  }
}