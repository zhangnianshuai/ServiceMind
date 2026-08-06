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

export type KnowledgeSourceType =
  | 'TEXT'
  | 'MARKDOWN';

export type KnowledgeVersionStatus =
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

export interface KnowledgeVersionSummary {
  versionNo: number;
  sourceType: KnowledgeSourceType;
  originalFilename: string | null;
  mimeType: string | null;
  contentHash: string;
  status: KnowledgeVersionStatus;
  chunkCount: number;
  embeddingModel: string | null;
  embeddingDimensions: number | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
  processedAt: string | null;
  publishedAt: string | null;
}

export interface KnowledgeDocumentDetail
  extends KnowledgeDocumentSummary {
  versions: KnowledgeVersionSummary[];
}

export interface KnowledgeDocumentFilters {
  category?: KnowledgeDocumentCategory;
  status?: KnowledgeDocumentStatus;
}

export interface CreateKnowledgeDocumentRequest {
  title: string;
  category: KnowledgeDocumentCategory;
  sourceType: KnowledgeSourceType;
  originalFilename: string | null;
  mimeType: string | null;
  rawContent: string;
  createdBy: string | null;
}
export interface KnowledgePublishResponse {
  documentNo: string;
  versionNo: number;
  documentStatus: KnowledgeDocumentStatus;
  versionStatus: KnowledgeVersionStatus;
  chunkCount: number;
  embeddingModel: string;
  embeddingDimensions: number;
  processedAt: string;
  publishedAt: string;
}