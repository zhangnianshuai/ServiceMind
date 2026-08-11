import {
  Component,
  inject,
  signal
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import {
  MatFormFieldModule
} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  KnowledgeDocumentService
} from '../../services/knowledge-document.service';
import {
  KnowledgeDocumentCategory,
  KnowledgeSearchResponse
} from '../../types/knowledge-document.types';

@Component({
  selector: 'app-knowledge-search',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './knowledge-search.component.html',
  styleUrl: './knowledge-search.component.scss'
})
export class KnowledgeSearchComponent {
  private readonly formBuilder = inject(FormBuilder);

  private readonly knowledgeDocumentService =
    inject(KnowledgeDocumentService);

  readonly expanded = signal(false);
  readonly isSearching = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly result = signal<KnowledgeSearchResponse | null>(null);

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

  readonly searchForm =
    this.formBuilder.nonNullable.group({
      query: [
        '',
        [
          Validators.required,
          Validators.maxLength(500)
        ]
      ],
      category: [
        '' as KnowledgeDocumentCategory | ''
      ],
      topK: [
        5,
        [
          Validators.required,
          Validators.min(1),
          Validators.max(20)
        ]
      ],
      similarityThreshold: [
        0.35,
        [
          Validators.required,
          Validators.min(0),
          Validators.max(1)
        ]
      ]
    });

  toggleExpanded(): void {
    this.expanded.update(value => !value);
  }

  search(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    const value = this.searchForm.getRawValue();
    const query = value.query.trim();

    if (!query) {
      this.searchForm.controls.query.setErrors({
        required: true
      });
      return;
    }

    this.isSearching.set(true);
    this.errorMessage.set(null);
    this.result.set(null);

    this.knowledgeDocumentService
      .searchKnowledge({
        query,
        category: value.category || undefined,
        topK: value.topK,
        similarityThreshold:
          value.similarityThreshold
      })
      .pipe(
        finalize(() => this.isSearching.set(false))
      )
      .subscribe({
        next: response => {
          this.result.set(response);
        },
        error: error => {
          this.errorMessage.set(
            this.knowledgeDocumentService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  clear(): void {
    this.searchForm.reset({
      query: '',
      category: '',
      topK: 5,
      similarityThreshold: 0.35
    });

    this.errorMessage.set(null);
    this.result.set(null);
  }

  categoryLabel(
    category: KnowledgeDocumentCategory
  ): string {
    return this.categories.find(
      item => item.value === category
    )?.label ?? category;
  }

  scoreLabel(
    score: number
  ): string {
    return Number.isFinite(score)
      ? score.toFixed(3)
      : '—';
  }
}