import {
  Component,
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
  MatTableModule
} from '@angular/material/table';

import {
  MatToolbarModule
} from '@angular/material/toolbar';

import {
  MatTooltipModule
} from '@angular/material/tooltip';

import {
  OrderService
} from '../../services/order.service';

import {
  OrderDetail,
  OrderStatus,
  OrderSummary
} from '../../types/order.types';

@Component({
  selector: 'app-orders',
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
    MatTableModule,
    MatToolbarModule,
    MatTooltipModule
  ],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss'
})
export class OrdersComponent {
  private readonly orderService =
    inject(OrderService);

  private readonly formBuilder =
    inject(FormBuilder);

  readonly customerOrders =
    signal<OrderSummary[]>([]);

  readonly selectedOrder =
    signal<OrderDetail | null>(null);

  readonly isCustomerSearching =
    signal(false);

  readonly isOrderLoading =
    signal(false);

  readonly customerSearchCompleted =
    signal(false);

  readonly customerError =
    signal<string | null>(null);

  readonly orderError =
    signal<string | null>(null);

  readonly orderColumns = [
    'orderNo',
    'customer',
    'status',
    'totalAmount',
    'orderedAt',
    'actions'
  ];

  readonly itemColumns = [
    'sku',
    'productName',
    'unitPrice',
    'quantity',
    'lineAmount'
  ];

  readonly customerSearchForm =
    this.formBuilder.nonNullable.group({
      customerNo: [
        '',
        [
          Validators.required,
          Validators.maxLength(32)
        ]
      ]
    });

  readonly orderSearchForm =
    this.formBuilder.nonNullable.group({
      orderNo: [
        '',
        [
          Validators.required,
          Validators.maxLength(32)
        ]
      ]
    });

  searchByCustomer(): void {
    if (this.customerSearchForm.invalid) {
      this.customerSearchForm.markAllAsTouched();
      return;
    }

    const customerNo =
      this.customerSearchForm.controls
        .customerNo.value.trim();

    if (!customerNo) {
      return;
    }

    this.isCustomerSearching.set(true);
    this.customerSearchCompleted.set(false);
    this.customerError.set(null);
    this.orderError.set(null);
    this.customerOrders.set([]);
    this.selectedOrder.set(null);

    this.orderService
      .getByCustomerNo(customerNo)
      .pipe(
        finalize(() => {
          this.isCustomerSearching.set(false);
        })
      )
      .subscribe({
        next: orders => {
          this.customerOrders.set(orders);
          this.customerSearchCompleted.set(true);
        },

        error: error => {
          this.customerSearchCompleted.set(true);

          this.customerError.set(
            this.orderService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  searchByOrderNo(): void {
    if (this.orderSearchForm.invalid) {
      this.orderSearchForm.markAllAsTouched();
      return;
    }

    const orderNo =
      this.orderSearchForm.controls
        .orderNo.value.trim();

    if (!orderNo) {
      return;
    }

    this.loadOrder(orderNo);
  }

  openOrder(
    order: OrderSummary
  ): void {
    this.loadOrder(order.orderNo);
  }

  clearCustomerSearch(): void {
    this.customerSearchForm.reset({
      customerNo: ''
    });

    this.customerOrders.set([]);
    this.customerSearchCompleted.set(false);
    this.customerError.set(null);
  }

  closeOrderDetail(): void {
    this.selectedOrder.set(null);
    this.orderError.set(null);
  }

  statusLabel(
    status: OrderStatus
  ): string {
    const labels: Record<OrderStatus, string> = {
      CREATED: '已创建',
      PAID: '已支付',
      SHIPPED: '已发货',
      COMPLETED: '已完成',
      CANCELLED: '已取消'
    };

    return labels[status];
  }

  statusClass(
    status: OrderStatus
  ): string {
    return `status-${status.toLowerCase()}`;
  }

  private loadOrder(
    orderNo: string
  ): void {
    this.isOrderLoading.set(true);
    this.orderError.set(null);
    this.selectedOrder.set(null);

    this.orderService
      .getByOrderNo(orderNo)
      .pipe(
        finalize(() => {
          this.isOrderLoading.set(false);
        })
      )
      .subscribe({
        next: order => {
          this.selectedOrder.set(order);
        },

        error: error => {
          this.orderError.set(
            this.orderService
              .resolveErrorMessage(error)
          );
        }
      });
  }
}