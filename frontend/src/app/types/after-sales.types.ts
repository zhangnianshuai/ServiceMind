export type AfterSalesType =
  | 'RETURN'
  | 'EXCHANGE'
  | 'REPAIR';

export type AfterSalesStatus =
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'CANCELLED';

export interface CreateAfterSalesRequest {
  orderNo: string;
  orderItemId: number;
  type: AfterSalesType;
  requestedQuantity: number;
  reason: string;
  description: string | null;
}

export interface ReviewAfterSalesRequest {
  comment: string | null;
}

export interface AfterSalesApplication {
  applicationNo: string;
  orderNo: string;
  orderItemId: number;
  sku: string;
  productName: string;
  type: AfterSalesType;
  status: AfterSalesStatus;
  requestedQuantity: number;
  reason: string;
  description: string | null;
  reviewComment: string | null;
  submittedAt: string;
  reviewedAt: string | null;
  completedAt: string | null;
  warrantyExpiresAt: string | null;
}