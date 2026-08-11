export type TicketCategory =
  | 'ORDER'
  | 'PAYMENT'
  | 'DELIVERY'
  | 'RETURN_EXCHANGE'
  | 'REPAIR'
  | 'PRODUCT'
  | 'ACCOUNT'
  | 'OTHER';

export type TicketPriority =
  | 'LOW'
  | 'NORMAL'
  | 'HIGH'
  | 'URGENT';

export type TicketSource =
  | 'WEB'
  | 'CHAT'
  | 'PHONE'
  | 'EMAIL'
  | 'AI'
  | 'SYSTEM';

export type TicketStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'WAITING_CUSTOMER'
  | 'RESOLVED'
  | 'CLOSED';

export interface CreateSupportTicketRequest {
  customerNo: string;
  orderNo: string | null;
  afterSalesApplicationNo: string | null;
  category: TicketCategory;
  priority: TicketPriority | null;
  source: TicketSource;
  subject: string;
  description: string;
}

export interface SupportTicket {
  ticketNo: string;
  customerNo: string;
  customerName: string;
  orderNo: string | null;
  afterSalesApplicationNo: string | null;
  category: TicketCategory;
  priority: TicketPriority;
  status: TicketStatus;
  source: TicketSource;
  subject: string;
  description: string;
  assignedTo: string | null;
  resolution: string | null;
  openedAt: string;
  firstRespondedAt: string | null;
  assignedAt: string | null;
  resolvedAt: string | null;
  closedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface SupportTicketPageResponse {
  content: SupportTicket[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}