import {
  AfterViewInit,
  Component,
  ViewChild,
  effect,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';

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
  KnowledgeDocumentService
} from '../../services/knowledge-document.service';
import {
  KnowledgeDocumentCategory,
  KnowledgeDocumentStatus,
  KnowledgeDocumentSummary
} from '../../types/knowledge-document.types';

@Component({
  selector: 'app-knowledge-documents',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatCardModule,
    MatToolbarModule,
    MatTooltipModule,
    MatProgressSpinnerModule
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

  readonly documents =
    this.knowledgeDocumentService.documents;

  readonly isLoading =
    this.knowledgeDocumentService.isLoading;

  readonly errorMessage =
    this.knowledgeDocumentService.errorMessage;

  readonly displayedColumns = [
    'documentNo',
    'title',
    'category',
    'status',
    'latestVersionNo',
    'activeVersionNo',
    'createdBy',
    'updatedAt'
  ];

  readonly dataSource =
    new MatTableDataSource<KnowledgeDocumentSummary>();

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

  categoryLabel(
    category: KnowledgeDocumentCategory
  ): string {
    const labels:
      Record<KnowledgeDocumentCategory, string> = {
        PRODUCT: '产品',
        WARRANTY: '保修',
        RETURN_EXCHANGE: '退换货',
        REPAIR: '维修',
        ORDER_PAYMENT: '订单与支付',
        ACCOUNT: '账户',
        GENERAL: '通用'
      };

    return labels[category];
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
}