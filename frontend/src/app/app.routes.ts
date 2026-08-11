import { Routes } from '@angular/router';

import {
  KnowledgeDocumentsComponent
} from './components/knowledge-documents/knowledge-documents.component';

export const routes: Routes = [
  {
    path: 'knowledge',
    component: KnowledgeDocumentsComponent,
    title: '知识库 - ServiceMind'
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'knowledge'
  },
  {
    path: '**',
    redirectTo: 'knowledge'
  }
];