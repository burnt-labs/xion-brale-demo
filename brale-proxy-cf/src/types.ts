export interface Env {
  DB: D1Database;
  TOKEN_CACHE: KVNamespace;
  BRALE_CLIENT_ID: string;
  BRALE_CLIENT_SECRET: string;
  BRALE_ACCOUNT_ID: string;
  BRALE_API_URL: string;
  BRALE_AUTH_URL: string;
  ALLOWED_TRANSFER_TYPES: string;
}
