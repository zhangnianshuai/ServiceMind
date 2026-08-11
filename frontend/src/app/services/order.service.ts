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
  CreateOrderRequest,
  OrderDetail,
  OrderPageResponse,
  OrderStatus,
  OrderSummary
} from '../types/order.types';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly httpClient =
    inject(HttpClient);

  private readonly apiUrl =
    '/api/orders';

  searchOrders(
    orderNo: string,
    customerNo: string,
    status: OrderStatus | '',
    page: number,
    size: number
  ): Observable<OrderPageResponse> {
    let params =
      new HttpParams()
        .set('page', page)
        .set('size', size);

    const normalizedOrderNo =
      orderNo.trim();

    const normalizedCustomerNo =
      customerNo.trim();

    if (normalizedOrderNo) {
      params = params.set(
        'orderNo',
        normalizedOrderNo
      );
    }

    if (normalizedCustomerNo) {
      params = params.set(
        'customerNo',
        normalizedCustomerNo
      );
    }

    if (status) {
      params = params.set(
        'status',
        status
      );
    }

    return this.httpClient
      .get<OrderPageResponse>(
        `${this.apiUrl}/search`,
        { params }
      );
  }

  getByOrderNo(
    orderNo: string
  ): Observable<OrderDetail> {
    return this.httpClient.get<OrderDetail>(
      `${this.apiUrl}/${encodeURIComponent(orderNo)}`
    );
  }

  getByCustomerNo(
    customerNo: string
  ): Observable<OrderSummary[]> {
    const params = new HttpParams()
      .set('customerNo', customerNo);

    return this.httpClient.get<OrderSummary[]>(
      this.apiUrl,
      { params }
    );
  }

  create(
    request: CreateOrderRequest
  ): Observable<OrderDetail> {
    return this.httpClient.post<OrderDetail>(
      this.apiUrl,
      request
    );
  }

  pay(orderNo: string): Observable<OrderDetail> {
    return this.postOrderAction(orderNo, 'pay');
  }

  ship(orderNo: string): Observable<OrderDetail> {
    return this.postOrderAction(orderNo, 'ship');
  }

  complete(orderNo: string): Observable<OrderDetail> {
    return this.postOrderAction(orderNo, 'complete');
  }

  cancel(orderNo: string): Observable<OrderDetail> {
    return this.postOrderAction(orderNo, 'cancel');
  }

  resolveErrorMessage(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return '订单操作失败，请稍后重试。';
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
      return '没有找到对应的数据。';
    }

    if (error.status === 409) {
      return '当前订单状态不允许执行此操作。';
    }

    return `订单操作失败，服务器返回状态码 ${error.status}。`;
  }

  private postOrderAction(
    orderNo: string,
    action: string
  ): Observable<OrderDetail> {
    return this.httpClient.post<OrderDetail>(
      `${this.apiUrl}/${encodeURIComponent(orderNo)}/${action}`,
      {}
    );
  }
}