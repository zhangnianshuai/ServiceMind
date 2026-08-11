export type OrderStatus =
  | 'CREATED'
  | 'PAID'
  | 'SHIPPED'
  | 'COMPLETED'
  | 'CANCELLED';

export interface OrderItem {
  orderItemId: number;
  sku: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineAmount: number;
}

export interface OrderSummary {
  orderNo: string;
  customerNo: string;
  customerName: string;
  status: OrderStatus;
  totalAmount: number;
  orderedAt: string;
}

export interface OrderDetail extends OrderSummary {
  paidAt: string | null;
  shippedAt: string | null;
  completedAt: string | null;
  items: OrderItem[];
}

export interface OrderPageResponse {
  content: OrderSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface CreateOrderItemRequest {
  sku: string;
  quantity: number;
}

export interface CreateOrderRequest {
  customerNo: string;
  items: CreateOrderItemRequest[];
}