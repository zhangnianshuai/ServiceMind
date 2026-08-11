import {
  Component,
  ElementRef,
  ViewChild,
  computed,
  effect,
  inject,
  signal
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  finalize
} from 'rxjs';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatCardModule
} from '@angular/material/card';

import {
  MatChipsModule
} from '@angular/material/chips';

import {
  MatFormFieldModule
} from '@angular/material/form-field';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatInputModule
} from '@angular/material/input';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  MatTooltipModule
} from '@angular/material/tooltip';

import {
  ChatService
} from '../../services/chat.service';

import {
  ModelSessionService
} from '../../services/model-session.service';

import {
  ChatUiMessage,
  CredentialSource,
  ModelSessionResponse
} from '../../types/chat.types';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent {
  @ViewChild('messagesContainer')
  private messagesContainer?:
    ElementRef<HTMLElement>;

  private readonly chatService =
    inject(ChatService);

  private readonly modelSessionService =
    inject(ModelSessionService);

  readonly modelSession =
    signal<ModelSessionResponse | null>(null);

  readonly messages =
    signal<ChatUiMessage[]>([
      this.createWelcomeMessage()
    ]);

  readonly chatId =
    signal<string | null>(null);

  readonly isSending =
    signal(false);

  readonly isConnecting =
    signal(false);

  readonly isRevoking =
    signal(false);

  readonly showModelSettings =
    signal(false);

  readonly chatError =
    signal<string | null>(null);

  readonly modelError =
    signal<string | null>(null);

  currentMessage = '';

  byokApiKey = '';

  readonly modeLabel =
    computed(() =>
      this.modelSession()
        ? 'BYOK'
        : '平台模型'
    );

  readonly modeDescription =
    computed(() =>
      this.modelSession()
        ? '用户临时凭证'
        : 'ServiceMind 平台凭证'
    );

  constructor() {
    effect(() => {
      this.messages();
      this.isSending();

      setTimeout(() => {
        this.scrollToBottom();
      });
    });
  }

  sendMessage(): void {
    const text =
      this.currentMessage.trim();

    if (!text || this.isSending()) {
      return;
    }

    this.currentMessage = '';
    this.chatError.set(null);

    this.messages.update(messages => [
      ...messages,
      {
        id: this.createId(),
        content: text,
        role: 'USER',
        timestamp: new Date()
      }
    ]);

    this.isSending.set(true);

    this.chatService
      .sendMessage(
        text,
        this.chatId(),
        this.modelSession()?.sessionId ?? null
      )
      .pipe(
        finalize(() => {
          this.isSending.set(false);
        })
      )
      .subscribe({
        next: response => {
          this.chatId.set(
            response.chatId
          );

          this.messages.update(messages => [
            ...messages,
            {
              id: this.createId(),
              content: response.text,
              role: 'ASSISTANT',
              timestamp: new Date(),
              credentialSource:
                response.credentialSource
            }
          ]);
        },

        error: error => {
          this.chatError.set(
            this.chatService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  connectByok(): void {
    const apiKey =
      this.byokApiKey.trim();

    if (!apiKey) {
      this.modelError.set(
        '请输入 DashScope API Key。'
      );

      return;
    }

    this.isConnecting.set(true);
    this.modelError.set(null);

    this.modelSessionService
      .create(apiKey)
      .pipe(
        finalize(() => {
          this.isConnecting.set(false);

          /*
           * API Key 不在前端长期保留。
           * 无论成功失败，请求结束后都立即清空。
           */
          this.byokApiKey = '';
        })
      )
      .subscribe({
        next: session => {
          this.modelSession.set(session);

          this.showModelSettings.set(false);

          this.startNewConversation();
        },

        error: error => {
          this.modelError.set(
            this.modelSessionService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  usePlatformMode(): void {
    const session =
      this.modelSession();

    if (!session) {
      this.showModelSettings.set(false);
      return;
    }

    this.isRevoking.set(true);
    this.modelError.set(null);

    this.modelSessionService
      .revoke(session.sessionId)
      .pipe(
        finalize(() => {
          this.isRevoking.set(false);
        })
      )
      .subscribe({
        next: () => {
          this.modelSession.set(null);

          this.showModelSettings.set(false);

          this.startNewConversation();
        },

        error: error => {
          this.modelError.set(
            this.modelSessionService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  disconnectByok(): void {
    this.usePlatformMode();
  }

  startNewConversation(): void {
    this.chatId.set(null);
    this.chatError.set(null);

    this.messages.set([
      this.createWelcomeMessage()
    ]);
  }

  toggleModelSettings(): void {
    this.modelError.set(null);

    this.showModelSettings.update(
      value => !value
    );
  }

  onKeydown(
    event: KeyboardEvent
  ): void {
    if (
      event.key === 'Enter' &&
      !event.shiftKey
    ) {
      event.preventDefault();

      this.sendMessage();
    }
  }

  credentialLabel(
    source?: CredentialSource
  ): string {
    if (source === 'USER_SESSION') {
      return 'BYOK';
    }

    if (source === 'PLATFORM') {
      return '平台';
    }

    return '';
  }

  private createWelcomeMessage():
      ChatUiMessage {
    const content =
      this.modelSession()
        ? '已进入 BYOK 模式。你可以继续询问知识库、订单、售后或工单问题。'
        : '你好，我是 ServiceMind 智能客服。你可以询问知识库、订单、售后或工单相关问题。';

    return {
      id: this.createId(),
      content,
      role: 'ASSISTANT',
      timestamp: new Date()
    };
  }

  private createId(): string {
    if (
      typeof crypto !== 'undefined' &&
      typeof crypto.randomUUID === 'function'
    ) {
      return crypto.randomUUID();
    }

    return `${Date.now()}-${Math.random()}`;
  }

  private scrollToBottom(): void {
    const element =
      this.messagesContainer
        ?.nativeElement;

    if (!element) {
      return;
    }

    element.scrollTop =
      element.scrollHeight;
  }
}