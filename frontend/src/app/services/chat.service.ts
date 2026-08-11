import {
  HttpClient,
  HttpErrorResponse
} from '@angular/common/http';

import {
  Injectable,
  inject
} from '@angular/core';

import {
  Observable
} from 'rxjs';

import {
  ChatRequest,
  ChatResponse
} from '../types/chat.types';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly httpClient =
    inject(HttpClient);

  private readonly apiUrl =
    '/api/customer-support';

  sendMessage(
    text: string,
    chatId: string | null,
    modelSessionId: string | null
  ): Observable<ChatResponse> {
    const request: ChatRequest = {
      text,
      chatId,
      modelSessionId
    };

    return this.httpClient.post<ChatResponse>(
      this.apiUrl,
      request
    );
  }

  resolveErrorMessage(
    error: unknown
  ): string {
    if (!(error instanceof HttpErrorResponse)) {
      return '智能客服暂时无法响应，请稍后重试。';
    }

    if (error.status === 0) {
      return '无法连接 ServiceMind 后端服务。';
    }

    if (
      error.error &&
      typeof error.error === 'object'
    ) {
      const payload =
        error.error as Record<string, unknown>;

      if (typeof payload['detail'] === 'string') {
        return payload['detail'];
      }

      if (typeof payload['message'] === 'string') {
        return payload['message'];
      }
    }

    if (error.status === 404) {
      return '当前 BYOK 会话不存在或已经失效。';
    }

    if (error.status === 401) {
      return '模型凭证无效或没有模型访问权限。';
    }

    return `智能客服请求失败，状态码 ${error.status}。`;
  }
}