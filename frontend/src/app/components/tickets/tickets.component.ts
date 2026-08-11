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
  TicketService
} from '../../services/ticket.service';

import {
  CreateSupportTicketRequest,
  SupportTicket,
  TicketCategory,
  TicketPriority,
  TicketSource,
  TicketStatus
} from '../../types/ticket.types';

@Component({
  selector: 'app-tickets',
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
    MatToolbarModule
  ],
  templateUrl: './tickets.component.html',
  styleUrl: './tickets.component.scss'
})
export class TicketsComponent {
  private readonly ticketService =
    inject(TicketService);

  readonly tickets =
    signal<SupportTicket[]>([]);

  readonly selectedTicket =
    signal<SupportTicket | null>(null);

  readonly isCreating =
    signal(false);

  readonly isSearching =
    signal(false);

  readonly isChanging =
    signal(false);

  readonly searchCompleted =
    signal(false);

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

  readonly categories:
      TicketCategory[] = [
        'ORDER',
        'PAYMENT',
        'DELIVERY',
        'RETURN_EXCHANGE',
        'REPAIR',
        'PRODUCT',
        'ACCOUNT',
        'OTHER'
      ];

  readonly priorities:
      TicketPriority[] = [
        'LOW',
        'NORMAL',
        'HIGH',
        'URGENT'
      ];

  readonly sources:
      TicketSource[] = [
        'WEB',
        'CHAT',
        'PHONE',
        'EMAIL',
        'AI',
        'SYSTEM'
      ];

  readonly createForm =
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

      orderNo: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.maxLength(32)
          ]
        }
      ),

      afterSalesApplicationNo:
        new FormControl(
          '',
          {
            nonNullable: true,
            validators: [
              Validators.maxLength(32)
            ]
          }
        ),

      category:
        new FormControl<TicketCategory>(
          'OTHER',
          {
            nonNullable: true,
            validators: [
              Validators.required
            ]
          }
        ),

      priority:
        new FormControl<TicketPriority>(
          'NORMAL',
          {
            nonNullable: true
          }
        ),

      source:
        new FormControl<TicketSource>(
          'WEB',
          {
            nonNullable: true,
            validators: [
              Validators.required
            ]
          }
        ),

      subject:
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
              Validators.required,
              Validators.maxLength(2000)
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
            Validators.required
          ]
        }
      )
    });

  readonly ticketSearchForm =
    new FormGroup({
      ticketNo: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.required
          ]
        }
      )
    });

  readonly assignmentForm =
    new FormGroup({
      assignedTo: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.required,
            Validators.maxLength(100)
          ]
        }
      )
    });

  readonly priorityForm =
    new FormGroup({
      priority:
        new FormControl<TicketPriority>(
          'NORMAL',
          {
            nonNullable: true,
            validators: [
              Validators.required
            ]
          }
        )
    });

  readonly resolutionForm =
    new FormGroup({
      resolution: new FormControl(
        '',
        {
          nonNullable: true,
          validators: [
            Validators.required,
            Validators.maxLength(2000)
          ]
        }
      )
    });

  createTicket(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const raw =
      this.createForm.getRawValue();

    const request:
        CreateSupportTicketRequest = {
      customerNo:
        raw.customerNo.trim(),

      orderNo:
        raw.orderNo.trim() || null,

      afterSalesApplicationNo:
        raw.afterSalesApplicationNo
          .trim() || null,

      category:
        raw.category,

      priority:
        raw.priority,

      source:
        raw.source,

      subject:
        raw.subject.trim(),

      description:
        raw.description.trim()
    };

    this.isCreating.set(true);
    this.createError.set(null);
    this.createSuccess.set(null);

    this.ticketService
      .create(request)
      .pipe(
        finalize(() => {
          this.isCreating.set(false);
        })
      )
      .subscribe({
        next: ticket => {
          this.selectTicket(ticket);

          this.createSuccess.set(
            `工单 ${ticket.ticketNo} 创建成功。`
          );

          this.createForm.reset({
            customerNo: '',
            orderNo: '',
            afterSalesApplicationNo: '',
            category: 'OTHER',
            priority: 'NORMAL',
            source: 'WEB',
            subject: '',
            description: ''
          });
        },

        error: error => {
          this.createError.set(
            this.ticketService
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
    this.searchCompleted.set(false);
    this.searchError.set(null);
    this.tickets.set([]);

    this.ticketService
      .getByCustomerNo(customerNo)
      .pipe(
        finalize(() => {
          this.isSearching.set(false);
        })
      )
      .subscribe({
        next: tickets => {
          this.tickets.set(tickets);
          this.searchCompleted.set(true);
        },

        error: error => {
          this.searchCompleted.set(true);

          this.searchError.set(
            this.ticketService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  searchByTicketNo(): void {
    if (this.ticketSearchForm.invalid) {
      this.ticketSearchForm.markAllAsTouched();
      return;
    }

    const ticketNo =
      this.ticketSearchForm.controls
        .ticketNo.value.trim();

    this.isSearching.set(true);
    this.searchError.set(null);

    this.ticketService
      .getByTicketNo(ticketNo)
      .pipe(
        finalize(() => {
          this.isSearching.set(false);
        })
      )
      .subscribe({
        next: ticket => {
          this.selectTicket(ticket);
        },

        error: error => {
          this.selectedTicket.set(null);

          this.searchError.set(
            this.ticketService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  selectTicket(
    ticket: SupportTicket
  ): void {
    this.selectedTicket.set(ticket);

    this.assignmentForm.reset({
      assignedTo:
        ticket.assignedTo ?? ''
    });

    this.priorityForm.reset({
      priority:
        ticket.priority
    });

    this.resolutionForm.reset({
      resolution:
        ticket.resolution ?? ''
    });

    this.actionError.set(null);
    this.actionSuccess.set(null);
  }

  assign(): void {
    const ticket =
      this.selectedTicket();

    if (
      !ticket ||
      this.assignmentForm.invalid
    ) {
      this.assignmentForm.markAllAsTouched();
      return;
    }

    this.runAction(
      this.ticketService.assign(
        ticket.ticketNo,
        this.assignmentForm.controls
          .assignedTo.value.trim()
      ),
      '工单分配成功。'
    );
  }

  changePriority(): void {
    const ticket =
      this.selectedTicket();

    if (!ticket) {
      return;
    }

    this.runAction(
      this.ticketService.changePriority(
        ticket.ticketNo,
        this.priorityForm.controls
          .priority.value
      ),
      '工单优先级已更新。'
    );
  }

  startProgress(): void {
    const ticket =
      this.selectedTicket();

    if (!ticket) {
      return;
    }

    this.runAction(
      this.ticketService.startProgress(
        ticket.ticketNo
      ),
      '工单已进入处理中。'
    );
  }

  waitForCustomer(): void {
    const ticket =
      this.selectedTicket();

    if (!ticket) {
      return;
    }

    this.runAction(
      this.ticketService.waitForCustomer(
        ticket.ticketNo
      ),
      '工单已进入等待客户状态。'
    );
  }

  resolve(): void {
    const ticket =
      this.selectedTicket();

    if (
      !ticket ||
      this.resolutionForm.invalid
    ) {
      this.resolutionForm.markAllAsTouched();
      return;
    }

    this.runAction(
      this.ticketService.resolve(
        ticket.ticketNo,
        this.resolutionForm.controls
          .resolution.value.trim()
      ),
      '工单已解决。'
    );
  }

  reopen(): void {
    const ticket =
      this.selectedTicket();

    if (!ticket) {
      return;
    }

    this.runAction(
      this.ticketService.reopen(
        ticket.ticketNo
      ),
      '工单已重新打开并进入处理中。'
    );
  }

  close(): void {
    const ticket =
      this.selectedTicket();

    if (!ticket) {
      return;
    }

    this.runAction(
      this.ticketService.close(
        ticket.ticketNo
      ),
      '工单已关闭。'
    );
  }

  canManage(
    ticket: SupportTicket
  ): boolean {
    return ticket.status !== 'CLOSED';
  }

  canStartProgress(
    status: TicketStatus
  ): boolean {
    return (
      status === 'OPEN' ||
      status === 'WAITING_CUSTOMER'
    );
  }

  canWaitForCustomer(
    status: TicketStatus
  ): boolean {
    return status === 'IN_PROGRESS';
  }

  canResolve(
    status: TicketStatus
  ): boolean {
    return (
      status === 'IN_PROGRESS' ||
      status === 'WAITING_CUSTOMER'
    );
  }

  canReopen(
    status: TicketStatus
  ): boolean {
    return status === 'RESOLVED';
  }

  canClose(
    status: TicketStatus
  ): boolean {
    return status === 'RESOLVED';
  }

  statusLabel(
    status: TicketStatus
  ): string {
    const labels:
        Record<TicketStatus, string> = {
      OPEN: '待处理',
      IN_PROGRESS: '处理中',
      WAITING_CUSTOMER: '等待客户',
      RESOLVED: '已解决',
      CLOSED: '已关闭'
    };

    return labels[status];
  }

  priorityLabel(
    priority: TicketPriority
  ): string {
    const labels:
        Record<TicketPriority, string> = {
      LOW: '低',
      NORMAL: '普通',
      HIGH: '高',
      URGENT: '紧急'
    };

    return labels[priority];
  }

  categoryLabel(
    category: TicketCategory
  ): string {
    const labels:
        Record<TicketCategory, string> = {
      ORDER: '订单',
      PAYMENT: '支付',
      DELIVERY: '物流',
      RETURN_EXCHANGE: '退换货',
      REPAIR: '维修',
      PRODUCT: '商品',
      ACCOUNT: '账户',
      OTHER: '其他'
    };

    return labels[category];
  }

  sourceLabel(
    source: TicketSource
  ): string {
    const labels:
        Record<TicketSource, string> = {
      WEB: '网页',
      CHAT: '在线聊天',
      PHONE: '电话',
      EMAIL: '邮件',
      AI: 'AI',
      SYSTEM: '系统'
    };

    return labels[source];
  }

  private runAction(
    request: Observable<SupportTicket>,
    successMessage: string
  ): void {
    this.isChanging.set(true);
    this.actionError.set(null);
    this.actionSuccess.set(null);

    request
      .pipe(
        finalize(() => {
          this.isChanging.set(false);
        })
      )
      .subscribe({
        next: ticket => {
          this.selectTicket(ticket);
          this.syncTicket(ticket);

          this.actionSuccess.set(
            successMessage
          );
        },

        error: error => {
          this.actionError.set(
            this.ticketService
              .resolveErrorMessage(error)
          );
        }
      });
  }

  private syncTicket(
    ticket: SupportTicket
  ): void {
    const current = this.tickets();

    const index =
      current.findIndex(
        item =>
          item.ticketNo ===
          ticket.ticketNo
      );

    if (index < 0) {
      return;
    }

    const updated = [...current];

    updated[index] = ticket;

    this.tickets.set(updated);
  }
}