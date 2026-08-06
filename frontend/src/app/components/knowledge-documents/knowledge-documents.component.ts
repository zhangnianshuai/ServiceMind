import {
  AfterViewInit,
  Component,
  ViewChild,
  effect,
  inject,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { finalize } from 'rxjs';

import {
  MatTableDataSource,
  MatTableModule
} from '@angular/material/table';
import {
  MatSort,
  MatSortModule
} from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';
import {
  MatFormFieldModule
} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import {
  KnowledgeDocumentService
} from '../../services/knowledge-document.service';
import {
  KnowledgeDocumentDetailComponent
} from '../knowledge-document-detail/knowledge-document-detail.component';
import {
  CreateKnowledgeDocumentRequest,
  KnowledgeDocumentCategory,
  KnowledgeDocumentStatus,
  KnowledgeDocumentSummary,
  KnowledgeSourceType
} from '../../types/knowledge-document.types';

@Component({
  selector: 'app-knowledge-documents',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatCardModule,
    MatToolbarModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    KnowledgeDocumentDetailComponent
  ],
  templateUrl:
    './knowledge-documents.component.html',
  styleUrl:
    './knowledge-documents.component.scss'
})
export class KnowledgeDocumentsComponent
    implements AfterViewInit {
  @ViewChild(MatSort)
  private sort!: MatSort;

  private readonly knowledgeDocumentService =
    inject(KnowledgeDocumentService);

  private readonly formBuilder =
    inject(FormBuilder);

  readonly documents =
    this.knowledgeDocumentService.documents;

  readonly isLoading =
    this.knowledgeDocumentService.isLoading;

  readonly errorMessage =
    this.knowledgeDocumentService.errorMessage;

  readonly showCreateForm = signal(false);
  readonly isCreating = signal(false);
  readonly createError = signal<string | null>(null);
  readonly createSuccess = signal<string | null>(null);
  readonly selectedDocumentNo = signal<string | null>(null);

  readonly categories: ReadonlyArray<{
    value: KnowledgeDocumentCategory;
    label: string;
  }> = [
    { value: 'PRODUCT', label: '产品' },
    { value: 'WARRANTY', label: '保修' },
    { value: 'RETURN_EXCHANGE', label: '退换货' },
    { value: 'REPAIR', label: '维修' },
    { value: 'ORDER_PAYMENT', label: '订单与支付' },
    { value: 'ACCOUNT', label: '账户' },
    { value: 'GENERAL', label: '通用' }
  ];

  readonly sourceTypes: ReadonlyArray<{
    value: KnowledgeSourceType;
    label: string;
  }> = [
    { value: 'TEXT', label: '纯文本' },
    { value: 'MARKDOWN', label: 'Markdown' }
  ];

  readonly displayedColumns = [
    'documentNo',
    'title',
    'category',
    'status',
    'latestVersionNo',
    'activeVersionNo',
    'createdBy',
    'updatedAt',
    'actions'
  ];

  readonly dataSource =
    new MatTableDataSource<KnowledgeDocumentSummary>();

  readonly createForm =
    this.formBuilder.nonNullable.group({
      title: [
        '',
        [
          Validators.required,
          Validators.maxLength(200)
        ]
      ],
      category: [
        'GENERAL' as KnowledgeDocumentCategory,
        Validators.required
      ],
      sourceType: [
        'TEXT' as KnowledgeSourceType,
        Validators.required
      ],
      rawContent: [
        '',
        [
          Validators.required,
          Validators.maxLength(1_000_000)
        ]
      ],
      createdBy: [
        '',
        Validators.maxLength(100)
      ]
    });

  constructor() {
    effect(() => {
      this.dataSource.data = this.documents();
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }

  refreshDocuments(): void {
    this.knowledgeDocumentService.refreshDocuments();
  }

  openDocumentDetail(
    document: KnowledgeDocumentSummary
  ): void {
    this.selectedDocumentNo.set(document.documentNo);
  }

  closeDocumentDetail(): void {
    this.selectedDocumentNo.set(null);
  }

  onDocumentChanged(): void {
    this.refreshDocuments();
  }

  openCreateForm(): void {
    this.createError.set(null);
    this.createSuccess.set(null);
    this.showCreateForm.set(true);
  }

  cancelCreate(): void {
    this.showCreateForm.set(false);
    this.createError.set(null);
    this.resetCreateForm();
  }

  submitCreateDocument(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const value = this.createForm.getRawValue();

    const request: CreateKnowledgeDocumentRequest = {
      title: value.title.trim(),
      category: value.category,
      sourceType: value.sourceType,
      originalFilename: null,
      mimeType:
        value.sourceType === 'MARKDOWN'
          ? 'text/markdown'
          : 'text/plain',
      rawContent: value.rawContent.trim(),
      createdBy: value.createdBy.trim() || null
    };

    this.isCreating.set(true);
    this.createError.set(null);
    this.createSuccess.set(null);

    this.knowledgeDocumentService
      .createDocument(request)
      .pipe(
        finalize(() => this.isCreating.set(false))
      )
      .subscribe({
        next: document => {
          this.createSuccess.set(
            `已创建知识文档“${document.title}”，当前为草稿版本 v${document.latestVersionNo}。`
          );

          this.showCreateForm.set(false);
          this.resetCreateForm();
          this.refreshDocuments();
        },
        error: error => {
          this.createError.set(
            this.knowledgeDocumentService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  categoryLabel(
    category: KnowledgeDocumentCategory
  ): string {
    return this.categories.find(
      item => item.value === category
    )?.label ?? category;
  }

  statusLabel(
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

  statusClass(
    status: KnowledgeDocumentStatus
  ): string {
    return `status-${status.toLowerCase()}`;
  }

  private resetCreateForm(): void {
    this.createForm.reset({
      title: '',
      category: 'GENERAL',
      sourceType: 'TEXT',
      rawContent: '',
      createdBy: ''
    });
  }
}