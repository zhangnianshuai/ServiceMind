import {
  HttpClient,
  HttpErrorResponse,
  HttpParams
} from '@angular/common/http';

import {
  Injectable,
  inject
} from '@angular/core';

import {
  Observable
} from 'rxjs';

import {
  CreateSupportTicketRequest,
  SupportTicket,
  SupportTicketPageResponse,
  TicketCategory,
  TicketPriority,
  TicketStatus
} from '../types/ticket.types';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private readonly httpClient =
    inject(HttpClient);

  private readonly apiUrl =
    '/api/tickets';

  search(
    ticketNo: string,
    customerNo: string,
    assignedTo: string,
    category: TicketCategory | '',
    priority: TicketPriority | '',
    status: TicketStatus | '',
    page: number,
    size: number
  ): Observable<SupportTicketPageResponse> {
    let params =
      new HttpParams()
        .set('page', page)
        .set('size', size);

    if (ticketNo.trim()) {
      params = params.set(
        'ticketNo',
        ticketNo.trim()
      );
    }

    if (customerNo.trim()) {
      params = params.set(
        'customerNo',
        customerNo.trim()
      );
    }

    if (assignedTo.trim()) {
      params = params.set(
        'assignedTo',
        assignedTo.trim()
      );
    }

    if (category) {
      params = params.set(
        'category',
        category
      );
    }

    if (priority) {
      params = params.set(
        'priority',
        priority
      );
    }

    if (status) {
      params = params.set(
        'status',
        status
      );
    }

    return this.httpClient
      .get<SupportTicketPageResponse>(
        `${this.apiUrl}/search`,
        { params }
      );
  }

  create(
    request: CreateSupportTicketRequest
  ): Observable<SupportTicket> {
    return this.httpClient.post<SupportTicket>(
      this.apiUrl,
      request
    );
  }

  getByTicketNo(
    ticketNo: string
  ): Observable<SupportTicket> {
    return this.httpClient.get<SupportTicket>(
      `${this.apiUrl}/${encodeURIComponent(ticketNo)}`
    );
  }

  getByCustomerNo(
    customerNo: string
  ): Observable<SupportTicket[]> {
    const params = new HttpParams()
      .set('customerNo', customerNo);

    return this.httpClient.get<SupportTicket[]>(
      this.apiUrl,
      { params }
    );
  }

  assign(
    ticketNo: string,
    assignedTo: string
  ): Observable<SupportTicket> {
    return this.httpClient.post<SupportTicket>(
      `${this.apiUrl}/${encodeURIComponent(ticketNo)}/assign`,
      { assignedTo }
    );
  }

  changePriority(
    ticketNo: string,
    priority: TicketPriority
  ): Observable<SupportTicket> {
    return this.httpClient.post<SupportTicket>(
      `${this.apiUrl}/${encodeURIComponent(ticketNo)}/priority`,
      { priority }
    );
  }

  startProgress(
    ticketNo: string
  ): Observable<SupportTicket> {
    return this.postAction(
      ticketNo,
      'start-progress'
    );
  }

  waitForCustomer(
    ticketNo: string
  ): Observable<SupportTicket> {
    return this.postAction(
      ticketNo,
      'wait-customer'
    );
  }

  resolve(
    ticketNo: string,
    resolution: string
  ): Observable<SupportTicket> {
    return this.httpClient.post<SupportTicket>(
      `${this.apiUrl}/${encodeURIComponent(ticketNo)}/resolve`,
      { resolution }
    );
  }

  reopen(
    ticketNo: string
  ): Observable<SupportTicket> {
    return this.postAction(
      ticketNo,
      'reopen'
    );
  }

  close(
    ticketNo: string
  ): Observable<SupportTicket> {
    return this.postAction(
      ticketNo,
      'close'
    );
  }

  resolveErrorMessage(
    error: unknown
  ): string {
    if (!(error instanceof HttpErrorResponse)) {
      return '工单操作失败，请稍后重试。';
    }

    if (error.status === 0) {
      return '无法连接后端服务，请确认 ServiceMind 后端已经启动。';
    }

    if (
      error.error &&
      typeof error.error === 'object'
    ) {
      const payload =
        error.error as Record<string, unknown>;

      if (typeof payload['detail'] === 'string') {
        return payload['detail'];
      }

      if (typeof payload['message'] === 'string') {
        return payload['message'];
      }

      if (typeof payload['error'] === 'string') {
        return payload['error'];
      }
    }

    if (error.status === 404) {
      return '没有找到对应的客户、订单、售后申请或工单。';
    }

    if (error.status === 409) {
      return '当前工单状态不允许执行此操作。';
    }

    return `工单操作失败，服务器返回状态码 ${error.status}。`;
  }

  private postAction(
    ticketNo: string,
    action: string
  ): Observable<SupportTicket> {
    return this.httpClient.post<SupportTicket>(
      `${this.apiUrl}/${encodeURIComponent(ticketNo)}/${action}`,
      {}
    );
  }
}