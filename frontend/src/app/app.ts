import { Component } from '@angular/core';

import {
  KnowledgeDocumentsComponent
} from './components/knowledge-documents/knowledge-documents.component';
import {
  ChatComponent
} from './components/chat/chat.component';

@Component({
  selector: 'app-root',
  imports: [
    KnowledgeDocumentsComponent,
    ChatComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = 'ServiceMind';
}