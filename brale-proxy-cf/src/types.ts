export interface Env {
  DB: D1Database;
  TOKEN_CACHE: KVNamespace;
  BRALE_CLIENT_ID: string;
  BRALE_CLIENT_SECRET: string;
  BRALE_ACCOUNT_ID: string;
  BRALE_API_URL: string;
  BRALE_AUTH_URL: string;
  ALLOWED_TRANSFER_TYPES: string;
  BRALE_CUSTOMER_WEBHOOK_URL: string;
  // XION LCD REST base, used to verify session-key authz grants for wallet auth.
  XION_REST_URL: string;
  // "true" => reject requests without valid signed wallet auth. "false" (default)
  // => soft mode: verify+scope when a valid signature is present, else legacy behavior.
  REQUIRE_WALLET_AUTH: string;
}
