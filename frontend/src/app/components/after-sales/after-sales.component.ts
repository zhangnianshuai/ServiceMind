import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  Observable,
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
  MatSelectModule
} from '@angular/material/select';

import {
  MatToolbarModule
} from '@angular/material/toolbar';

import {
  MatTooltipModule
} from '@angular/material/tooltip';

import {
  AfterSalesService
} from '../../services/after-sales.service';

import {
  OrderService
} from '../../services/order.service';

import {
  AfterSalesApplication,
  AfterSalesStatus,
  AfterSalesType,
  CreateAfterSalesRequest
} from '../../types/after-sales.types';

import {
  OrderDetail
} from '../../types/order.types';

@Component({
  selector: 'app-after-sales',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatToolbarModule,
    MatTooltipModule
  ],
  templateUrl: './after-sales.component.html',
  styleUrl: './after-sales.component.scss'
})
export class AfterSalesComponent {
  private readonly afterSalesService =
    inject(AfterSalesService);

  private readonly orderService =
    inject(OrderService);

  readonly loadedOrder =
    signal<OrderDetail | null>(null);

  readonly selectedApplication =
    signal<AfterSalesApplication | null>(null);

  readonly customerApplications =
    signal<AfterSalesApplication[]>([]);

  readonly isOrderLoading =
    signal(false);

  readonly isCreating =
    signal(false);

  readonly isSearching =
    signal(false);

  readonly isChangingState =
    signal(false);

  readonly orderError =
    signal<string | null>(null);

  readonly createError =
    signal<string | null>(null);

  readonly createSuccess =
    signal<string | null>(null);

  readonly searchError =
    signal<string | null>(null);

  readonly actionError =
    signal<string | null>(null);

  readonly actionSuccess =
    signal<string | null>(null);

  readonly customerSearchCompleted =
    signal(false);

  readonly orderLoadForm =
    new FormGroup({
      orderNo: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.required,
            Validators.maxLength(32)
          ]
        }
      )
    });

  readonly createForm =
    new FormGroup({
      orderItemId:
        new FormControl<number | null>(
          null,
          [
            Validators.required,
            Validators.min(1)
          ]
        ),

      type:
        new FormControl<AfterSalesType | null>(
          null,
          Validators.required
        ),

      requestedQuantity:
        new FormControl(
          1,
          {
            nonNullable: true,
            validators: [
              Validators.required,
              Validators.min(1)
            ]
          }
        ),

      reason:
        new FormControl(
          '',
          {
            nonNullable: true,
            validators: [
              Validators.required,
              Validators.maxLength(200)
            ]
          }
        ),

      description:
        new FormControl(
          '',
          {
            nonNullable: true,
            validators: [
              Validators.maxLength(1000)
            ]
          }
        )
    });

  readonly customerSearchForm =
    new FormGroup({
      customerNo: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.required,
            Validators.maxLength(32)
          ]
        }
      )
    });

  readonly applicationSearchForm =
    new FormGroup({
      applicationNo: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.required,
            Validators.maxLength(32)
          ]
        }
      )
    });

  readonly reviewForm =
    new FormGroup({
      comment: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.maxLength(500)
          ]
        }
      )
    });

  loadOrder(): void {
    if (this.orderLoadForm.invalid) {
      this.orderLoadForm.markAllAsTouched();
      return;
    }

    const orderNo =
      this.orderLoadForm.controls
        .orderNo.value.trim();

    this.isOrderLoading.set(true);
    this.orderError.set(null);
    this.createError.set(null);
    this.createSuccess.set(null);
    this.loadedOrder.set(null);

    this.orderService
      .getByOrderNo(orderNo)
      .pipe(
        finalize(() => {
          this.isOrderLoading.set(false);
        })
      )
      .subscribe({
        next: order => {
          this.loadedOrder.set(order);

          this.createForm.reset({
            orderItemId: null,
            type: null,
            requestedQuantity: 1,
            reason: '',
            description: ''
          });
        },

        error: error => {
          this.orderError.set(
            this.orderService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  createApplication(): void {
    const order = this.loadedOrder();

    if (!order) {
      this.createError.set(
        '请先加载需要申请售后的订单。'
      );
      return;
    }

    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const raw =
      this.createForm.getRawValue();

    if (
      raw.orderItemId === null ||
      raw.type === null
    ) {
      return;
    }

    const request: CreateAfterSalesRequest = {
      orderNo: order.orderNo,
      orderItemId: raw.orderItemId,
      type: raw.type,
      requestedQuantity:
        raw.requestedQuantity,
      reason: raw.reason.trim(),
      description:
        raw.description.trim() || null
    };

    this.isCreating.set(true);
    this.createError.set(null);
    this.createSuccess.set(null);

    this.afterSalesService
      .create(request)
      .pipe(
        finalize(() => {
          this.isCreating.set(false);
        })
      )
      .subscribe({
        next: application => {
          this.selectedApplication.set(
            application
          );

          this.createSuccess.set(
            `售后申请 ${application.applicationNo} 创建成功。`
          );
        },

        error: error => {
          this.createError.set(
            this.afterSalesService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  searchByCustomer(): void {
    if (this.customerSearchForm.invalid) {
      this.customerSearchForm.markAllAsTouched();
      return;
    }

    const customerNo =
      this.customerSearchForm.controls
        .customerNo.value.trim();

    this.isSearching.set(true);
    this.searchError.set(null);
    this.customerSearchCompleted.set(false);
    this.customerApplications.set([]);

    this.afterSalesService
      .getByCustomerNo(customerNo)
      .pipe(
        finalize(() => {
          this.isSearching.set(false);
        })
      )
      .subscribe({
        next: applications => {
          this.customerApplications.set(
            applications
          );

          this.customerSearchCompleted.set(
            true
          );
        },

        error: error => {
          this.customerSearchCompleted.set(
            true
          );

          this.searchError.set(
            this.afterSalesService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  searchByApplicationNo(): void {
    if (this.applicationSearchForm.invalid) {
      this.applicationSearchForm.markAllAsTouched();
      return;
    }

    const applicationNo =
      this.applicationSearchForm.controls
        .applicationNo.value.trim();

    this.isSearching.set(true);
    this.searchError.set(null);

    this.afterSalesService
      .getByApplicationNo(applicationNo)
      .pipe(
        finalize(() => {
          this.isSearching.set(false);
        })
      )
      .subscribe({
        next: application => {
          this.selectApplication(application);
        },

        error: error => {
          this.selectedApplication.set(null);

          this.searchError.set(
            this.afterSalesService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  selectApplication(
    application: AfterSalesApplication
  ): void {
    this.selectedApplication.set(
      application
    );

    this.reviewForm.reset({
      comment:
        application.reviewComment ?? ''
    });

    this.actionError.set(null);
    this.actionSuccess.set(null);
  }

  startReview(): void {
    const application =
      this.selectedApplication();

    if (!application) {
      return;
    }

    this.runAction(
      this.afterSalesService.startReview(
        application.applicationNo
      ),
      '售后申请已进入审核。'
    );
  }

  approve(): void {
    const application =
      this.selectedApplication();

    if (!application) {
      return;
    }

    const comment =
      this.reviewForm.controls
        .comment.value.trim();

    this.runAction(
      this.afterSalesService.approve(
        application.applicationNo,
        comment || null
      ),
      '售后申请审核通过。'
    );
  }

  reject(): void {
    const application =
      this.selectedApplication();

    if (!application) {
      return;
    }

    const comment =
      this.reviewForm.controls
        .comment.value.trim();

    if (!comment) {
      this.actionError.set(
        '拒绝申请时必须填写审核意见。'
      );
      return;
    }

    this.runAction(
      this.afterSalesService.reject(
        application.applicationNo,
        comment
      ),
      '售后申请已拒绝。'
    );
  }

  startProcessing(): void {
    const application =
      this.selectedApplication();

    if (!application) {
      return;
    }

    this.runAction(
      this.afterSalesService.startProcessing(
        application.applicationNo
      ),
      '售后申请已进入处理阶段。'
    );
  }

  complete(): void {
    const application =
      this.selectedApplication();

    if (!application) {
      return;
    }

    this.runAction(
      this.afterSalesService.complete(
        application.applicationNo
      ),
      '售后处理已完成。'
    );
  }

  cancel(): void {
    const application =
      this.selectedApplication();

    if (!application) {
      return;
    }

    this.runAction(
      this.afterSalesService.cancel(
        application.applicationNo
      ),
      '售后申请已取消。'
    );
  }

  eligibleTypes(
    order: OrderDetail
  ): AfterSalesType[] {
    if (order.status === 'COMPLETED') {
      return [
        'RETURN',
        'EXCHANGE',
        'REPAIR'
      ];
    }

    if (order.status === 'SHIPPED') {
      return [
        'RETURN',
        'EXCHANGE'
      ];
    }

    return [];
  }

  canStartReview(
    status: AfterSalesStatus
  ): boolean {
    return status === 'SUBMITTED';
  }

  canReview(
    status: AfterSalesStatus
  ): boolean {
    return status === 'UNDER_REVIEW';
  }

  canStartProcessing(
    status: AfterSalesStatus
  ): boolean {
    return status === 'APPROVED';
  }

  canComplete(
    status: AfterSalesStatus
  ): boolean {
    return status === 'PROCESSING';
  }

  canCancel(
    status: AfterSalesStatus
  ): boolean {
    return (
      status === 'SUBMITTED' ||
      status === 'UNDER_REVIEW'
    );
  }

  statusLabel(
    status: AfterSalesStatus
  ): string {
    const labels:
      Record<AfterSalesStatus, string> = {
        SUBMITTED: '已提交',
        UNDER_REVIEW: '审核中',
        APPROVED: '已通过',
        REJECTED: '已拒绝',
        PROCESSING: '处理中',
        COMPLETED: '已完成',
        CANCELLED: '已取消'
      };

    return labels[status];
  }

  typeLabel(
    type: AfterSalesType
  ): string {
    const labels:
      Record<AfterSalesType, string> = {
        RETURN: '退货',
        EXCHANGE: '换货',
        REPAIR: '维修'
      };

    return labels[type];
  }

  private runAction(
    request: Observable<AfterSalesApplication>,
    message: string
  ): void {
    this.isChangingState.set(true);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    request
      .pipe(
        finalize(() => {
          this.isChangingState.set(false);
        })
      )
      .subscribe({
        next: application => {
          this.selectedApplication.set(
            application
          );

          this.syncApplication(application);

          this.actionSuccess.set(message);
        },

        error: error => {
          this.actionError.set(
            this.afterSalesService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  private syncApplication(
    application: AfterSalesApplication
  ): void {
    const applications =
      this.customerApplications();

    const index =
      applications.findIndex(
        item =>
          item.applicationNo ===
          application.applicationNo
      );

    if (index < 0) {
      return;
    }

    const updated = [...applications];

    updated[index] = application;

    this.customerApplications.set(
      updated
    );
  }
}