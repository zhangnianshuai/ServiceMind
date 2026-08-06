import { Component, signal, ViewChild, ElementRef, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ChatService } from '../../services/chat.service';

interface Message {
  id: string;
  content: string;
  isUser: boolean;
  timestamp: Date;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent {
  // ViewChild reference to the messages container for auto-scrolling
  @ViewChild('messagesContainer') private readonly messagesContainer!: ElementRef;

  // Signals for reactive state management
  messages = signal<Message[]>([
    {
      id: '1',
      content: '您好，我是 ServiceMind 智能客服，请问有什么可以帮助您？',
      isUser: false,
      timestamp: new Date(),
    }
  ]);

  currentMessage = signal('');
  isLoading = signal(false);

  // Computed signal for display columns
  displayedColumns = signal(['bookingNumber', 'firstName', 'lastName', 'date', 'bookingStatus', 'from', 'to', 'seatNumber', 'bookingClass']);

  constructor(private readonly chatService: ChatService) {
    // Effect to auto-scroll when messages change
    effect(() => {
      // This effect runs whenever messages() signal changes
      this.messages();
      this.scrollToBottom();
    });
  }

  sendMessage() {
    const messageText = this.currentMessage().trim();
    if (!messageText) return;

    // Add user message
    const userMessage: Message = {
      id: Date.now().toString(),
      content: messageText,
      isUser: true,
      timestamp: new Date()
    };

    this.messages.update(messages => [...messages, userMessage]);
    this.currentMessage.set('');
    this.isLoading.set(true);

    // Send message to API
    this.chatService.sendMessage(messageText).subscribe({
      next: (response) => {
        const aiResponse: Message = {
          id: (Date.now() + 1).toString(),
          content: response.text,
          isUser: false,
          timestamp: new Date()
        };

        this.messages.update(messages => [...messages, aiResponse]);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error sending message:', error);

        // Fallback message if API fails
        const errorResponse: Message = {
          id: (Date.now() + 1).toString(),
          content: '暂时无法连接智能客服服务，请确认后端已经启动后重试。',
          isUser: false,
          timestamp: new Date()
        };

        this.messages.update(messages => [...messages, errorResponse]);
        this.isLoading.set(false);
      }
    });
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  // Scroll to the bottom of the messages container
  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer) {
        const container = this.messagesContainer.nativeElement;
        container.scrollTop = container.scrollHeight;
      }
    }, 100);
  }
}
