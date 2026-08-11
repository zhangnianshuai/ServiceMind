export type AuthRole =
  | 'ADMIN'
  | 'AGENT';

export interface AuthUser {
  username: string;
  displayName: string;
  role: AuthRole;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface CsrfResponse {
  headerName: string;
  token: string;
}