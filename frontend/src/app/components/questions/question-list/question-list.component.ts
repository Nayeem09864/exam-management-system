import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { QuestionService, Question } from '../../../services/question.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-question-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './question-list.component.html',
  styleUrls: ['./question-list.component.css']
})
export class QuestionListComponent implements OnInit {
  questions: Question[] = [];
  isLoading = false;
  errorMessage = '';
  
  // Filter options
  selectedDifficulty: string = '';
  selectedTopic: string = '';
  selectedDate: string = '';
  
  // Available filter values
  difficulties = ['EASY', 'MEDIUM', 'HARD'];
  topics: string[] = [];

  constructor(
    private questionService: QuestionService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.loadQuestions();
  }

  loadQuestions(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.questionService.getAllQuestions(
      this.selectedDifficulty || undefined,
      this.selectedTopic || undefined,
      this.selectedDate || undefined
    ).subscribe({
      next: (data) => {
        this.questions = data;
        this.extractTopics();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error loading questions';
        this.isLoading = false;
        console.error('Error:', error);
      }
    });
  }

  extractTopics(): void {
    const uniqueTopics = [...new Set(this.questions.map(q => q.topic))];
    this.topics = uniqueTopics.sort();
  }

  onFilterChange(): void {
    this.loadQuestions();
  }

  clearFilters(): void {
    this.selectedDifficulty = '';
    this.selectedTopic = '';
    this.selectedDate = '';
    this.loadQuestions();
  }

  deleteQuestion(id: number): void {
    if (confirm('Are you sure you want to delete this question?')) {
      this.questionService.deleteQuestion(id).subscribe({
        next: () => {
          this.loadQuestions();
        },
        error: (error) => {
          this.errorMessage = 'Error deleting question';
          console.error('Error:', error);
        }
      });
    }
  }

  editQuestion(id: number): void {
    this.router.navigate(['/questions/edit', id]);
  }

  getDifficultyColor(difficulty: string): string {
    switch (difficulty) {
      case 'EASY': return 'green';
      case 'MEDIUM': return 'orange';
      case 'HARD': return 'red';
      default: return 'gray';
    }
  }
}
