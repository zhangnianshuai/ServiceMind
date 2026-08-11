import {
  HttpClient,
  HttpErrorResponse,
  HttpParams
} from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  OrderDetail,
  OrderSummary
} from '../types/order.types';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly httpClient = inject(HttpClient);

  private readonly apiUrl = '/api/orders';

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

  resolveErrorMessage(
    error: unknown
  ): string {
    if (!(error instanceof HttpErrorResponse)) {
      return '订单查询失败，请稍后重试。';
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
    }

    if (error.status === 404) {
      return '没有找到对应的订单。';
    }

    return `订单查询失败，服务器返回状态码 ${error.status}。`;
  }
}