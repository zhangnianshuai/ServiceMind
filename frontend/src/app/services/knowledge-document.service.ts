import {
  HttpClient,
  HttpErrorResponse,
  HttpParams
} from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, finalize } from 'rxjs';

import {
  CreateKnowledgeDocumentRequest,
  KnowledgeDocumentDetail,
  KnowledgeDocumentFilters,
  KnowledgeDocumentSummary
} from '../types/knowledge-document.types';

@Injectable({
  providedIn: 'root'
})
export class KnowledgeDocumentService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiUrl = '/api/knowledge/documents';

  private readonly documentsState =
    signal<KnowledgeDocumentSummary[]>([]);

  private readonly loadingState = signal(false);
  private readonly errorState = signal<string | null>(null);

  readonly documents = this.documentsState.asReadonly();
  readonly isLoading = this.loadingState.asReadonly();
  readonly errorMessage = this.errorState.asReadonly();

  constructor() {
    this.refreshDocuments();
  }

  refreshDocuments(
    filters: KnowledgeDocumentFilters = {}
  ): void {
    let params = new HttpParams();

    if (filters.category) {
      params = params.set(
        'category',
        filters.category
      );
    }

    if (filters.status) {
      params = params.set(
        'status',
        filters.status
      );
    }

    this.loadingState.set(true);
    this.errorState.set(null);

    this.httpClient
      .get<KnowledgeDocumentSummary[]>(
        this.apiUrl,
        { params }
      )
      .pipe(
        finalize(() => this.loadingState.set(false))
      )
      .subscribe({
        next: documents => {
          this.documentsState.set(documents);
        },
        error: error => {
          console.error(
            'Failed to load knowledge documents.',
            error
          );

          this.documentsState.set([]);
          this.errorState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  createDocument(
    request: CreateKnowledgeDocumentRequest
  ): Observable<KnowledgeDocumentDetail> {
    return this.httpClient.post<KnowledgeDocumentDetail>(
      this.apiUrl,
      request
    );
  }

  resolveErrorMessage(
    error: unknown
  ): string {
    if (!(error instanceof HttpErrorResponse)) {
      return '操作失败，请稍后重试。';
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

      if (typeof payload['title'] === 'string') {
        return payload['title'];
      }
    }

    if (error.status === 0) {
      return '无法连接后端服务，请确认服务已经启动。';
    }

    return `操作失败，服务器返回状态码 ${error.status}。`;
  }
}