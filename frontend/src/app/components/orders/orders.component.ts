import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

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
  MatPaginatorModule,
  PageEvent
} from '@angular/material/paginator';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  MatSelectModule
} from '@angular/material/select';

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
  AdminOnlyDirective
} from '../../directives/admin-only.directive';

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
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    MatToolbarModule,
    MatTooltipModule,
    AdminOnlyDirective
  ],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.scss'
})
export class OrdersComponent implements OnInit {
  private readonly orderService =
    inject(OrderService);

  readonly orders =
    signal<OrderSummary[]>([]);

  readonly selectedOrder =
    signal<OrderDetail | null>(null);

  readonly isListLoading =
    signal(false);

  readonly isOrderLoading =
    signal(false);

  readonly isCreating =
    signal(false);

  readonly isStateChanging =
    signal(false);

  readonly listError =
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

  readonly pageIndex =
    signal(0);

  readonly pageSize =
    signal(20);

  readonly totalElements =
    signal(0);

  readonly totalPages =
    signal(0);

  readonly statuses: OrderStatus[] = [
    'CREATED',
    'PAID',
    'SHIPPED',
    'COMPLETED',
    'CANCELLED'
  ];

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

  readonly filterForm =
    new FormGroup({
      orderNo: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.maxLength(32)
          ]
        }
      ),

      customerNo: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.maxLength(32)
          ]
        }
      ),

      status:
        new FormControl<OrderStatus | ''>(
          '',
          {
            nonNullable: true
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

  ngOnInit(): void {
    this.loadOrders();
  }

  get createItems(): FormArray<OrderItemForm> {
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

  removeOrderItem(index: number): void {
    if (this.createItems.length <= 1) {
      return;
    }

    this.createItems.removeAt(index);
  }

  applyFilters(): void {
    if (this.filterForm.invalid) {
      this.filterForm.markAllAsTouched();
      return;
    }

    this.pageIndex.set(0);
    this.loadOrders();
  }

  resetFilters(): void {
    this.filterForm.reset({
      orderNo: '',
      customerNo: '',
      status: ''
    });

    this.pageIndex.set(0);
    this.loadOrders();
  }

  refreshOrders(): void {
    this.loadOrders();
  }

  onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadOrders();
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

          this.resetCreateForm();
          this.pageIndex.set(0);
          this.loadOrders();
        },

        error: error => {
          this.createError.set(
            this.orderService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  openOrder(order: OrderSummary): void {
    this.loadOrder(order.orderNo);
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

  canPay(order: OrderDetail): boolean {
    return order.status === 'CREATED';
  }

  canShip(order: OrderDetail): boolean {
    return order.status === 'PAID';
  }

  canComplete(order: OrderDetail): boolean {
    return order.status === 'SHIPPED';
  }

  canCancel(order: OrderDetail): boolean {
    return (
      order.status === 'CREATED' ||
      order.status === 'PAID'
    );
  }

  statusLabel(status: OrderStatus): string {
    const labels: Record<OrderStatus, string> = {
      CREATED: '已创建',
      PAID: '已支付',
      SHIPPED: '已发货',
      COMPLETED: '已完成',
      CANCELLED: '已取消'
    };

    return labels[status];
  }

  statusClass(status: OrderStatus): string {
    return `status-${status.toLowerCase()}`;
  }

  private loadOrders(): void {
    const filters =
      this.filterForm.getRawValue();

    this.isListLoading.set(true);
    this.listError.set(null);

    this.orderService
      .searchOrders(
        filters.orderNo,
        filters.customerNo,
        filters.status,
        this.pageIndex(),
        this.pageSize()
      )
      .pipe(
        finalize(() => {
          this.isListLoading.set(false);
        })
      )
      .subscribe({
        next: response => {
          this.orders.set(response.content);
          this.pageIndex.set(response.page);
          this.pageSize.set(response.size);
          this.totalElements.set(
            response.totalElements
          );
          this.totalPages.set(
            response.totalPages
          );
        },

        error: error => {
          this.orders.set([]);
          this.totalElements.set(0);
          this.totalPages.set(0);

          this.listError.set(
            this.orderService
              .resolveErrorMessage(error)
          );
        }
      });
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

  private loadOrder(orderNo: string): void {
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
          this.actionSuccess.set(successMessage);
          this.loadOrders();
        },

        error: error => {
          this.actionError.set(
            this.orderService
              .resolveErrorMessage(error)
          );
        }
      });
  }
}