import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { QuestionListComponent } from './components/questions/question-list/question-list.component';
import { QuestionFormComponent } from './components/questions/question-form/question-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'questions', component: QuestionListComponent },
  { path: 'questions/create', component: QuestionFormComponent },
  { path: 'questions/edit/:id', component: QuestionFormComponent },
  { path: '**', redirectTo: '/login' }
];
