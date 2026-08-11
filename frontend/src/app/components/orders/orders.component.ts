import {
  Component,
  inject,
  signal
} from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  FormArray,
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
  CreateOrderRequest,
  OrderDetail,
  OrderStatus,
  OrderSummary
} from '../../types/order.types';

type OrderItemForm = FormGroup<{
  sku: FormControl<string>;
  quantity: FormControl<number>;
}>;

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

  readonly customerOrders =
    signal<OrderSummary[]>([]);

  readonly selectedOrder =
    signal<OrderDetail | null>(null);

  readonly isCustomerSearching =
    signal(false);

  readonly isOrderLoading =
    signal(false);

  readonly isCreating =
    signal(false);

  readonly isStateChanging =
    signal(false);

  readonly customerSearchCompleted =
    signal(false);

  readonly customerError =
    signal<string | null>(null);

  readonly orderError =
    signal<string | null>(null);

  readonly createError =
    signal<string | null>(null);

  readonly createSuccess =
    signal<string | null>(null);

  readonly actionError =
    signal<string | null>(null);

  readonly actionSuccess =
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

  readonly orderSearchForm =
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

  readonly createOrderForm =
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
      ),

      items: new FormArray<OrderItemForm>([
        this.createOrderItemForm()
      ])
    });

  get createItems():
      FormArray<OrderItemForm> {
    return this.createOrderForm.controls.items;
  }

  addOrderItem(): void {
    if (this.createItems.length >= 50) {
      return;
    }

    this.createItems.push(
      this.createOrderItemForm()
    );
  }

  removeOrderItem(
    index: number
  ): void {
    if (this.createItems.length <= 1) {
      return;
    }

    this.createItems.removeAt(index);
  }

  createOrder(): void {
    if (this.createOrderForm.invalid) {
      this.createOrderForm.markAllAsTouched();
      return;
    }

    const request: CreateOrderRequest = {
      customerNo:
        this.createOrderForm.controls
          .customerNo.value.trim(),

      items:
        this.createItems.controls.map(
          item => ({
            sku:
              item.controls.sku.value.trim(),

            quantity:
              item.controls.quantity.value
          })
        )
    };

    this.isCreating.set(true);
    this.createError.set(null);
    this.createSuccess.set(null);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    this.orderService
      .create(request)
      .pipe(
        finalize(() => {
          this.isCreating.set(false);
        })
      )
      .subscribe({
        next: order => {
          this.selectedOrder.set(order);

          this.createSuccess.set(
            `订单 ${order.orderNo} 创建成功。`
          );

          this.syncCustomerOrder(
            order,
            true
          );

          this.resetCreateForm();
        },

        error: error => {
          this.createError.set(
            this.orderService
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

    if (!customerNo) {
      return;
    }

    this.isCustomerSearching.set(true);
    this.customerSearchCompleted.set(false);
    this.customerError.set(null);
    this.customerOrders.set([]);

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
    this.actionError.set(null);
    this.actionSuccess.set(null);
  }

  payOrder(): void {
    const order = this.selectedOrder();

    if (!order) {
      return;
    }

    this.runStateChange(
      this.orderService.pay(order.orderNo),
      '订单支付成功。'
    );
  }

  shipOrder(): void {
    const order = this.selectedOrder();

    if (!order) {
      return;
    }

    this.runStateChange(
      this.orderService.ship(order.orderNo),
      '订单发货成功。'
    );
  }

  completeOrder(): void {
    const order = this.selectedOrder();

    if (!order) {
      return;
    }

    this.runStateChange(
      this.orderService.complete(order.orderNo),
      '订单已完成。'
    );
  }

  cancelOrder(): void {
    const order = this.selectedOrder();

    if (!order) {
      return;
    }

    this.runStateChange(
      this.orderService.cancel(order.orderNo),
      '订单已取消。'
    );
  }

  canPay(
    order: OrderDetail
  ): boolean {
    return order.status === 'CREATED';
  }

  canShip(
    order: OrderDetail
  ): boolean {
    return order.status === 'PAID';
  }

  canComplete(
    order: OrderDetail
  ): boolean {
    return order.status === 'SHIPPED';
  }

  canCancel(
    order: OrderDetail
  ): boolean {
    return (
      order.status === 'CREATED' ||
      order.status === 'PAID'
    );
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

  private createOrderItemForm():
      OrderItemForm {
    return new FormGroup({
      sku: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.required,
            Validators.maxLength(64)
          ]
        }
      ),

      quantity: new FormControl(
        1,
        {
          nonNullable: true,
          validators: [
            Validators.required,
            Validators.min(1)
          ]
        }
      )
    });
  }

  private resetCreateForm(): void {
    this.createOrderForm.controls
      .customerNo.setValue('');

    this.createItems.clear();

    this.createItems.push(
      this.createOrderItemForm()
    );
  }

  private loadOrder(
    orderNo: string
  ): void {
    this.isOrderLoading.set(true);
    this.orderError.set(null);
    this.actionError.set(null);
    this.actionSuccess.set(null);

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
          this.selectedOrder.set(null);

          this.orderError.set(
            this.orderService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  private runStateChange(
    request: Observable<OrderDetail>,
    successMessage: string
  ): void {
    this.isStateChanging.set(true);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    request
      .pipe(
        finalize(() => {
          this.isStateChanging.set(false);
        })
      )
      .subscribe({
        next: order => {
          this.selectedOrder.set(order);

          this.syncCustomerOrder(
            order,
            false
          );

          this.actionSuccess.set(
            successMessage
          );
        },

        error: error => {
          this.actionError.set(
            this.orderService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  private syncCustomerOrder(
    order: OrderDetail,
    addIfMissing: boolean
  ): void {
    const currentCustomerNo =
      this.customerSearchForm.controls
        .customerNo.value.trim();

    if (
      !currentCustomerNo ||
      currentCustomerNo.toLowerCase() !==
        order.customerNo.toLowerCase()
    ) {
      return;
    }

    const summary: OrderSummary = {
      orderNo: order.orderNo,
      customerNo: order.customerNo,
      customerName: order.customerName,
      status: order.status,
      totalAmount: order.totalAmount,
      orderedAt: order.orderedAt
    };

    const orders = this.customerOrders();

    const index = orders.findIndex(
      item =>
        item.orderNo === order.orderNo
    );

    if (index >= 0) {
      const updated = [...orders];
      updated[index] = summary;

      this.customerOrders.set(updated);
      return;
    }

    if (addIfMissing) {
      this.customerOrders.set([
        summary,
        ...orders
      ]);
    }
  }
}