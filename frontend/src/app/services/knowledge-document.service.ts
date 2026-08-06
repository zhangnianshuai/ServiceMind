import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';

import {
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
      params = params.set('category', filters.category);
    }

    if (filters.status) {
      params = params.set('status', filters.status);
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

  private resolveErrorMessage(
    error: HttpErrorResponse
  ): string {
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

    return '知识文档加载失败，请确认后端服务已经启动。';
  }
}