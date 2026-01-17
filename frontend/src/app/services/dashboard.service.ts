import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DashboardData {
  totalExams: number;
  totalCandidates: number;
  totalQuestions: number;
  totalAttempts: number;
  submittedAttempts: number;
  exams: ExamSummary[];
  recentResults: ResultSummary[];
}

export interface ExamSummary {
  examId: number;
  examName: string;
  accessCode: string;
  totalQuestions: number;
  durationMinutes: number;
  totalAttempts: number;
  submittedAttempts: number;
  totalCandidates: number;
  averageScore?: number;
  isActive: boolean;
  createdAt: string;
}

export interface ResultSummary {
  resultId: number;
  examName: string;
  candidateName: string;
  candidateEmail: string;
  totalQuestions: number;
  correctAnswers: number;
  wrongAnswers: number;
  percentage: number;
  evaluatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8080/api/dashboard';

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<DashboardData> {
    return this.http.get<DashboardData>(this.apiUrl);
  }
}
