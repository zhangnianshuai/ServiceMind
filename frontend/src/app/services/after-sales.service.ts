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
  AfterSalesApplication,
  CreateAfterSalesRequest,
  ReviewAfterSalesRequest
} from '../types/after-sales.types';

@Injectable({
  providedIn: 'root'
})
export class AfterSalesService {
  private readonly httpClient =
    inject(HttpClient);

  private readonly apiUrl =
    '/api/after-sales';

  create(
    request: CreateAfterSalesRequest
  ): Observable<AfterSalesApplication> {
    return this.httpClient.post<AfterSalesApplication>(
      this.apiUrl,
      request
    );
  }

  getByApplicationNo(
    applicationNo: string
  ): Observable<AfterSalesApplication> {
    return this.httpClient.get<AfterSalesApplication>(
      `${this.apiUrl}/${encodeURIComponent(applicationNo)}`
    );
  }

  getByCustomerNo(
    customerNo: string
  ): Observable<AfterSalesApplication[]> {
    const params = new HttpParams()
      .set('customerNo', customerNo);

    return this.httpClient.get<AfterSalesApplication[]>(
      this.apiUrl,
      { params }
    );
  }

  startReview(
    applicationNo: string
  ): Observable<AfterSalesApplication> {
    return this.postAction(
      applicationNo,
      'start-review'
    );
  }

  approve(
    applicationNo: string,
    comment: string | null
  ): Observable<AfterSalesApplication> {
    const request: ReviewAfterSalesRequest = {
      comment
    };

    return this.httpClient.post<AfterSalesApplication>(
      `${this.apiUrl}/${encodeURIComponent(applicationNo)}/approve`,
      request
    );
  }

  reject(
    applicationNo: string,
    comment: string
  ): Observable<AfterSalesApplication> {
    const request: ReviewAfterSalesRequest = {
      comment
    };

    return this.httpClient.post<AfterSalesApplication>(
      `${this.apiUrl}/${encodeURIComponent(applicationNo)}/reject`,
      request
    );
  }

  startProcessing(
    applicationNo: string
  ): Observable<AfterSalesApplication> {
    return this.postAction(
      applicationNo,
      'start-processing'
    );
  }

  complete(
    applicationNo: string
  ): Observable<AfterSalesApplication> {
    return this.postAction(
      applicationNo,
      'complete'
    );
  }

  cancel(
    applicationNo: string
  ): Observable<AfterSalesApplication> {
    return this.postAction(
      applicationNo,
      'cancel'
    );
  }

  resolveErrorMessage(
    error: unknown
  ): string {
    if (!(error instanceof HttpErrorResponse)) {
      return '售后操作失败，请稍后重试。';
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
      return '没有找到对应的售后申请或订单。';
    }

    if (error.status === 409) {
      return '当前状态不允许执行此操作。';
    }

    return `售后操作失败，服务器返回状态码 ${error.status}。`;
  }

  private postAction(
    applicationNo: string,
    action: string
  ): Observable<AfterSalesApplication> {
    return this.httpClient.post<AfterSalesApplication>(
      `${this.apiUrl}/${encodeURIComponent(applicationNo)}/${action}`,
      {}
    );
  }
}