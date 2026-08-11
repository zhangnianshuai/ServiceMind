export type CredentialSource =
  | 'PLATFORM'
  | 'USER_SESSION';

export type ModelProvider =
  | 'DASHSCOPE';

export interface ChatRequest {
  text: string;
  chatId: string | null;
  modelSessionId: string | null;
}

export interface ChatResponse {
  text: string;
  chatId: string;
  credentialSource: CredentialSource;
}

export interface CreateModelSessionRequest {
  provider: ModelProvider;
  apiKey: string;
  model: 'qwen-plus';
}

export interface ModelSessionResponse {
  sessionId: string;
  provider: ModelProvider;
  model: string;
  expiresAt: string;
}

export interface ChatUiMessage {
  id: string;
  content: string;
  role: 'USER' | 'ASSISTANT';
  timestamp: Date;
  credentialSource?: CredentialSource;
}