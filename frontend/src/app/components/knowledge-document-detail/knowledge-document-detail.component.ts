import {
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';

import {
  KnowledgeDocumentService
} from '../../services/knowledge-document.service';
import {
  KnowledgeDocumentDetail,
  KnowledgeDocumentStatus,
  KnowledgeVersionStatus,
  KnowledgeVersionSummary
} from '../../types/knowledge-document.types';

@Component({
  selector: 'app-knowledge-document-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl:
    './knowledge-document-detail.component.html',
  styleUrl:
    './knowledge-document-detail.component.scss'
})
export class KnowledgeDocumentDetailComponent {
  readonly documentNo = input.required<string>();

  readonly closed = output<void>();
  readonly documentChanged = output<void>();

  private readonly knowledgeDocumentService =
    inject(KnowledgeDocumentService);

  readonly detail =
    signal<KnowledgeDocumentDetail | null>(null);

  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly publishSuccess = signal<string | null>(null);
  readonly publishingVersionNo = signal<number | null>(null);

  readonly latestVersion = computed(() => {
    const document = this.detail();

    if (!document) {
      return null;
    }

    return document.versions.find(
      version =>
        version.versionNo === document.latestVersionNo
    ) ?? null;
  });

  readonly canPublishLatest = computed(() => {
    const document = this.detail();
    const version = this.latestVersion();

    return (
      document !== null &&
      document.status !== 'ARCHIVED' &&
      version?.status === 'DRAFT'
    );
  });

  constructor() {
    effect(() => {
      const documentNo = this.documentNo();

      if (documentNo) {
        this.loadDocument(documentNo);
      }
    });
  }

  close(): void {
    this.closed.emit();
  }

  publishLatestVersion(): void {
    const document = this.detail();
    const version = this.latestVersion();

    if (
      !document ||
      !version ||
      !this.canPublishLatest()
    ) {
      return;
    }

    this.publishingVersionNo.set(version.versionNo);
    this.errorMessage.set(null);
    this.publishSuccess.set(null);

    this.knowledgeDocumentService
      .publishVersion(
        document.documentNo,
        version.versionNo
      )
      .pipe(
        finalize(() =>
          this.publishingVersionNo.set(null)
        )
      )
      .subscribe({
        next: result => {
          this.publishSuccess.set(
            `版本 v${result.versionNo} 已发布，` +
              `生成 ${result.chunkCount} 个知识切块。`
          );

          this.loadDocument(document.documentNo);
          this.documentChanged.emit();
        },
        error: error => {
          this.errorMessage.set(
            this.knowledgeDocumentService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  documentStatusLabel(
    status: KnowledgeDocumentStatus
  ): string {
    const labels:
      Record<KnowledgeDocumentStatus, string> = {
        DRAFT: '草稿',
        PROCESSING: '处理中',
        READY: '已发布',
        FAILED: '处理失败',
        ARCHIVED: '已归档'
      };

    return labels[status];
  }

  versionStatusLabel(
    status: KnowledgeVersionStatus
  ): string {
    const labels:
      Record<KnowledgeVersionStatus, string> = {
        DRAFT: '草稿',
        PROCESSING: '处理中',
        READY: '已发布',
        FAILED: '处理失败',
        ARCHIVED: '已归档'
      };

    return labels[status];
  }

  statusClass(
    status:
      | KnowledgeDocumentStatus
      | KnowledgeVersionStatus
  ): string {
    return `status-${status.toLowerCase()}`;
  }

  sourceTypeLabel(
    version: KnowledgeVersionSummary
  ): string {
    return version.sourceType === 'MARKDOWN'
      ? 'Markdown'
      : '纯文本';
  }

  isLatestVersion(
    version: KnowledgeVersionSummary
  ): boolean {
    return (
      version.versionNo ===
      this.detail()?.latestVersionNo
    );
  }

  isActiveVersion(
    version: KnowledgeVersionSummary
  ): boolean {
    return (
      version.versionNo ===
      this.detail()?.activeVersionNo
    );
  }

  private loadDocument(
    documentNo: string
  ): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.knowledgeDocumentService
      .findDocument(documentNo)
      .pipe(
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
        next: detail => {
          this.detail.set(detail);
        },
        error: error => {
          this.detail.set(null);
          this.errorMessage.set(
            this.knowledgeDocumentService
              .resolveErrorMessage(error)
          );
        }
      });
  }
}