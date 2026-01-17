import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface QuestionOption {
  optionIndex: number;
  optionText: string;
  optionImageUrl?: string;
}

export interface Question {
  id?: number;
  questionText: string;
  paragraph?: string;
  imageUrl?: string;
  options: QuestionOption[];
  correctAnswerIndices: number[];
  difficultyLevel: 'EASY' | 'MEDIUM' | 'HARD';
  topic: string;
  solution?: string;
  explanation?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class QuestionService {
  private apiUrl = 'http://localhost:8080/api/questions';

  constructor(private http: HttpClient) {}

  createQuestion(question: Question): Observable<Question> {
    return this.http.post<Question>(this.apiUrl, question);
  }

  updateQuestion(id: number, question: Question): Observable<Question> {
    return this.http.put<Question>(`${this.apiUrl}/${id}`, question);
  }

  getQuestionById(id: number): Observable<Question> {
    return this.http.get<Question>(`${this.apiUrl}/${id}`);
  }

  getAllQuestions(difficulty?: string, topic?: string, startDate?: string): Observable<Question[]> {
    let params = new HttpParams();
    if (difficulty) params = params.set('difficulty', difficulty);
    if (topic) params = params.set('topic', topic);
    if (startDate) params = params.set('startDate', startDate);

    return this.http.get<Question[]>(this.apiUrl, { params });
  }

  deleteQuestion(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
