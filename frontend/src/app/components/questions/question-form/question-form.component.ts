import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { QuestionService, Question } from '../../../services/question.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-question-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './question-form.component.html',
  styleUrls: ['./question-form.component.css']
})
export class QuestionFormComponent implements OnInit {
  questionForm: FormGroup;
  isEditMode = false;
  questionId: number | null = null;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  difficulties = ['EASY', 'MEDIUM', 'HARD'];
  minOptions = 2;

  constructor(
    private fb: FormBuilder,
    private questionService: QuestionService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.questionForm = this.createForm();
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.questionId = +id;
      this.loadQuestion(this.questionId);
    } else {
      // Start with 4 options by default
      this.addOption();
      this.addOption();
      this.addOption();
      this.addOption();
    }
  }

  createForm(): FormGroup {
    return this.fb.group({
      questionText: ['', [Validators.required]],
      paragraph: [''],
      imageUrl: [''],
      difficultyLevel: ['EASY', [Validators.required]],
      topic: ['', [Validators.required]],
      solution: [''],
      explanation: [''],
      options: this.fb.array([]),
      correctAnswers: this.fb.array([], [Validators.required, this.atLeastOneCorrectAnswer.bind(this)])
    });
  }

  atLeastOneCorrectAnswer(control: any): { [key: string]: any } | null {
    const formArray = control as FormArray;
    if (formArray.length === 0) {
      return { noCorrectAnswer: true };
    }
    return null;
  }

  get options(): FormArray {
    return this.questionForm.get('options') as FormArray;
  }

  get correctAnswers(): FormArray {
    return this.questionForm.get('correctAnswers') as FormArray;
  }

  addOption(): void {
    const optionForm = this.fb.group({
      optionText: ['', [Validators.required]],
      optionImageUrl: ['']
    });
    this.options.push(optionForm);
  }

  removeOption(index: number): void {
    if (this.options.length > this.minOptions) {
      this.options.removeAt(index);
      // Remove from correct answers if selected
      const correctIndex = this.correctAnswers.value.indexOf(index);
      if (correctIndex > -1) {
        this.correctAnswers.removeAt(correctIndex);
      }
      // Adjust correct answer indices if needed
      this.adjustCorrectAnswerIndices(index);
    }
  }

  adjustCorrectAnswerIndices(removedIndex: number): void {
    const values = this.correctAnswers.value;
    this.correctAnswers.clear();
    values.forEach((idx: number) => {
      if (idx > removedIndex) {
        this.correctAnswers.push(this.fb.control(idx - 1));
      } else if (idx < removedIndex) {
        this.correctAnswers.push(this.fb.control(idx));
      }
    });
  }

  toggleCorrectAnswer(index: number): void {
    const correctValues = this.correctAnswers.value;
    const idx = correctValues.indexOf(index);
    
    if (idx > -1) {
      this.correctAnswers.removeAt(idx);
    } else {
      this.correctAnswers.push(this.fb.control(index));
    }
  }

  isCorrectAnswer(index: number): boolean {
    return this.correctAnswers.value.includes(index);
  }

  loadQuestion(id: number): void {
    this.isLoading = true;
    this.questionService.getQuestionById(id).subscribe({
      next: (question) => {
        this.questionForm.patchValue({
          questionText: question.questionText,
          paragraph: question.paragraph || '',
          imageUrl: question.imageUrl || '',
          difficultyLevel: question.difficultyLevel,
          topic: question.topic,
          solution: question.solution || '',
          explanation: question.explanation || ''
        });

        // Clear existing options
        while (this.options.length !== 0) {
          this.options.removeAt(0);
        }

        // Add options
        question.options.forEach((option, index) => {
          const optionForm = this.fb.group({
            optionText: [option.optionText, [Validators.required]],
            optionImageUrl: [option.optionImageUrl || '']
          });
          this.options.push(optionForm);
        });

        // Set correct answers
        this.correctAnswers.clear();
        question.correctAnswerIndices.forEach(index => {
          this.correctAnswers.push(this.fb.control(index));
        });

        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Error loading question';
        this.isLoading = false;
        console.error('Error:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.questionForm.valid && this.correctAnswers.length > 0) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const formValue = this.questionForm.value;
      const question: Question = {
        questionText: formValue.questionText,
        paragraph: formValue.paragraph || undefined,
        imageUrl: formValue.imageUrl || undefined,
        difficultyLevel: formValue.difficultyLevel,
        topic: formValue.topic,
        solution: formValue.solution || undefined,
        explanation: formValue.explanation || undefined,
        options: formValue.options.map((opt: any, index: number) => ({
          optionIndex: index,
          optionText: opt.optionText,
          optionImageUrl: opt.optionImageUrl || undefined
        })),
        correctAnswerIndices: formValue.correctAnswers.sort((a: number, b: number) => a - b)
      };

      if (this.isEditMode && this.questionId) {
        this.questionService.updateQuestion(this.questionId, question).subscribe({
          next: () => {
            this.successMessage = 'Question updated successfully!';
            this.isLoading = false;
            setTimeout(() => {
              this.router.navigate(['/questions']);
            }, 1500);
          },
          error: (error) => {
            this.errorMessage = error.error?.message || 'Error updating question';
            this.isLoading = false;
          }
        });
      } else {
        this.questionService.createQuestion(question).subscribe({
          next: () => {
            this.successMessage = 'Question created successfully!';
            this.isLoading = false;
            setTimeout(() => {
              this.router.navigate(['/questions']);
            }, 1500);
          },
          error: (error) => {
            this.errorMessage = error.error?.message || 'Error creating question';
            this.isLoading = false;
          }
        });
      }
    } else {
      if (this.correctAnswers.length === 0) {
        this.errorMessage = 'Please select at least one correct answer';
      } else {
        this.errorMessage = 'Please fill in all required fields correctly';
      }
    }
  }

  cancel(): void {
    this.router.navigate(['/questions']);
  }
}
