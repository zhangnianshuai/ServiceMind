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
  CreateModelSessionRequest,
  ModelSessionResponse
} from '../types/chat.types';

@Injectable({
  providedIn: 'root'
})
export class ModelSessionService {
  private readonly httpClient =
    inject(HttpClient);

  private readonly apiUrl =
    '/api/model-sessions';

  create(
    apiKey: string
  ): Observable<ModelSessionResponse> {
    const request: CreateModelSessionRequest = {
      provider: 'DASHSCOPE',
      apiKey,
      model: 'qwen-plus'
    };

    return this.httpClient.post<ModelSessionResponse>(
      this.apiUrl,
      request
    );
  }

  revoke(
    sessionId: string
  ): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.apiUrl}/${encodeURIComponent(sessionId)}`
    );
  }

  resolveErrorMessage(
    error: unknown
  ): string {
    if (!(error instanceof HttpErrorResponse)) {
      return '无法创建临时模型会话。';
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

    if (error.status === 401) {
      return 'API Key 无效，或没有 qwen-plus 使用权限。';
    }

    if (error.status === 502) {
      return '模型供应商暂时无法完成凭证验证。';
    }

    return `BYOK 会话创建失败，状态码 ${error.status}。`;
  }
}