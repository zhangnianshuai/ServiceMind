export type KnowledgeDocumentCategory =
  | 'PRODUCT'
  | 'WARRANTY'
  | 'RETURN_EXCHANGE'
  | 'REPAIR'
  | 'ORDER_PAYMENT'
  | 'ACCOUNT'
  | 'GENERAL';

export type KnowledgeDocumentStatus =
  | 'DRAFT'
  | 'PROCESSING'
  | 'READY'
  | 'FAILED'
  | 'ARCHIVED';

export interface KnowledgeDocumentSummary {
  documentNo: string;
  title: string;
  category: KnowledgeDocumentCategory;
  status: KnowledgeDocumentStatus;
  latestVersionNo: number;
  activeVersionNo: number | null;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeDocumentFilters {
  category?: KnowledgeDocumentCategory;
  status?: KnowledgeDocumentStatus;
}