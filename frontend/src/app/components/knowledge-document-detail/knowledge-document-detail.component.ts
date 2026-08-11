import {
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  input,
  output,
  signal,
  ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
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
  MatFormFieldModule
} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import {
  AdminOnlyDirective
} from '../../directives/admin-only.directive';

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
    MatTooltipModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    AdminOnlyDirective
  ],
  templateUrl:
    './knowledge-document-detail.component.html',
  styleUrl:
    './knowledge-document-detail.component.scss'
})
export class KnowledgeDocumentDetailComponent {
  @ViewChild('versionFileInput')
  private versionFileInput?: ElementRef<HTMLInputElement>;

  readonly documentNo = input.required<string>();

  readonly closed = output<void>();
  readonly documentChanged = output<void>();

  private readonly knowledgeDocumentService =
    inject(KnowledgeDocumentService);

  private readonly formBuilder =
    inject(FormBuilder);

  readonly detail =
    signal<KnowledgeDocumentDetail | null>(null);

  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly publishSuccess = signal<string | null>(null);
  readonly publishingVersionNo = signal<number | null>(null);

  readonly selectedFile = signal<File | null>(null);
  readonly isUploading = signal(false);
  readonly uploadSuccess = signal<string | null>(null);

  readonly isEditing = signal(false);
  readonly isSavingMetadata = signal(false);
  readonly isArchiving = signal(false);
  readonly managementSuccess = signal<string | null>(null);

  readonly categories = [
    { value: 'PRODUCT', label: '产品' },
    { value: 'WARRANTY', label: '保修' },
    { value: 'RETURN_EXCHANGE', label: '退换货' },
    { value: 'REPAIR', label: '维修' },
    { value: 'ORDER_PAYMENT', label: '订单与支付' },
    { value: 'ACCOUNT', label: '账户' },
    { value: 'GENERAL', label: '通用' }
  ] as const;

  readonly editForm =
    this.formBuilder.nonNullable.group({
      title: [
        '',
        [
          Validators.required,
          Validators.maxLength(200)
        ]
      ],
      category: [
        'GENERAL' as
          import('../../types/knowledge-document.types')
            .KnowledgeDocumentCategory,
        Validators.required
      ]
    });

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

  startEditing(): void {
    const document = this.detail();

    if (!document || document.status === 'ARCHIVED') {
      return;
    }

    this.editForm.reset({
      title: document.title,
      category: document.category
    });

    this.errorMessage.set(null);
    this.managementSuccess.set(null);
    this.isEditing.set(true);
  }

  cancelEditing(): void {
    this.isEditing.set(false);
    this.editForm.reset();
  }

  saveMetadata(): void {
    const document = this.detail();

    if (!document) {
      return;
    }

    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    const value = this.editForm.getRawValue();

    this.isSavingMetadata.set(true);
    this.errorMessage.set(null);
    this.managementSuccess.set(null);

    this.knowledgeDocumentService
      .updateDocument(
        document.documentNo,
        {
          title: value.title.trim(),
          category: value.category
        }
      )
      .pipe(
        finalize(() =>
          this.isSavingMetadata.set(false)
        )
      )
      .subscribe({
        next: updated => {
          this.detail.set(updated);
          this.isEditing.set(false);

          this.managementSuccess.set(
            '文档标题和分类已更新。'
          );

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

  archiveDocument(): void {
    const document = this.detail();

    if (!document || document.status === 'ARCHIVED') {
      return;
    }

    const confirmed = window.confirm(
      `确定要归档“${document.title}”吗？` +
      '归档后不能再新增或发布版本。'
    );

    if (!confirmed) {
      return;
    }

    this.isArchiving.set(true);
    this.errorMessage.set(null);
    this.managementSuccess.set(null);

    this.knowledgeDocumentService
      .archiveDocument(document.documentNo)
      .pipe(
        finalize(() =>
          this.isArchiving.set(false)
        )
      )
      .subscribe({
        next: archived => {
          this.detail.set(archived);
          this.isEditing.set(false);

          this.managementSuccess.set(
            '知识文档已归档。'
          );

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

  onFileSelected(
    event: Event
  ): void {
    const input =
      event.target as HTMLInputElement;

    const file =
      input.files?.item(0) ?? null;

    this.errorMessage.set(null);
    this.uploadSuccess.set(null);

    if (!file) {
      this.selectedFile.set(null);
      return;
    }

    const filename = file.name.toLowerCase();

    const supportedExtension =
      filename.endsWith('.txt') ||
      filename.endsWith('.md') ||
      filename.endsWith('.markdown');

    if (!supportedExtension) {
      this.selectedFile.set(null);
      input.value = '';
      this.errorMessage.set(
        '只支持 TXT、MD 和 MARKDOWN 文件。'
      );
      return;
    }

    if (file.size <= 0) {
      this.selectedFile.set(null);
      input.value = '';
      this.errorMessage.set(
        '上传文件不能为空。'
      );
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      this.selectedFile.set(null);
      input.value = '';
      this.errorMessage.set(
        '上传文件不能超过 2 MB。'
      );
      return;
    }

    this.selectedFile.set(file);
  }

  uploadSelectedFile(): void {
    const document = this.detail();
    const file = this.selectedFile();

    if (!document || !file) {
      return;
    }

    if (document.status === 'ARCHIVED') {
      this.errorMessage.set(
        '已归档文档不能上传新版本。'
      );
      return;
    }

    this.isUploading.set(true);
    this.errorMessage.set(null);
    this.uploadSuccess.set(null);
    this.publishSuccess.set(null);

    this.knowledgeDocumentService
      .uploadVersion(
        document.documentNo,
        file
      )
      .pipe(
        finalize(() => this.isUploading.set(false))
      )
      .subscribe({
        next: version => {
          this.uploadSuccess.set(
            `文件“${file.name}”已上传为草稿版本 ` +
              `v${version.versionNo}。`
          );

          this.selectedFile.set(null);

          if (this.versionFileInput) {
            this.versionFileInput.nativeElement.value = '';
          }

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