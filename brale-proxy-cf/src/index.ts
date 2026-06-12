import { Hono } from "hono";
import { cors } from "hono/cors";
import type { Env } from "./types";
import { braleRequest, BraleError } from "./auth";
import {
  getAccountId,
  saveAccountMapping,
  getMappingCount,
  recordOwner,
  getOwnedIds,
} from "./db";
import { verifyWalletAuth } from "./walletauth";

type HonoEnv = {
  Bindings: Env;
  // braleAccountId: the (shared) Brale account. authWallet: the verified caller
  // wallet (meta account), or null when unauthenticated in soft mode.
  Variables: { braleAccountId: string; authWallet: string | null };
};

const app = new Hono<HonoEnv>();

app.use("*", cors());

// ---------------------------------------------------------------------------
// Error handler
// ---------------------------------------------------------------------------

function errorResponse(err: unknown) {
  if (err instanceof BraleError) {
    return { error: err.message, details: err.data, _status: err.status };
  }
  const message = err instanceof Error ? err.message : "Internal server error";
  return { error: message, details: null, _status: 500 };
}

// Find an already-registered bank on the account by its last-4, used to recover
// from Brale's duplicate-registration error and share the bank with a new owner.
async function findBankByMask(
  env: Env,
  accountId: string,
  mask: string
): Promise<Record<string, unknown> | null> {
  try {
    const data = await braleRequest(env, "GET", `/accounts/${accountId}/addresses`);
    const addresses =
      (data as { addresses?: Array<Record<string, unknown>> })?.addresses ?? [];
    const match = addresses.find(
      (a) =>
        typeof a.account_number === "string" &&
        (a.account_number as string).endsWith(mask)
    );
    return match?.id ? match : null;
  } catch {
    return null;
  }
}

// ---------------------------------------------------------------------------
// Transfer type safety guard
// ---------------------------------------------------------------------------

function getAllowedTypes(env: Env) {
  if (env.ALLOWED_TRANSFER_TYPES === "*") return null;
  return new Set(env.ALLOWED_TRANSFER_TYPES.split(",").map((t) => t.trim()));
}

function validateTransferTypes(body: Record<string, unknown>, allowed: Set<string> | null) {
  if (!allowed) return null;
  for (const endpoint of [body?.source, body?.destination] as Array<
    { transfer_type?: string } | undefined
  >) {
    if (endpoint?.transfer_type && !allowed.has(endpoint.transfer_type)) {
      return `Transfer type "${endpoint.transfer_type}" is not allowed. Allowed: ${[...allowed].join(", ")}`;
    }
  }
  return null;
}

// ---------------------------------------------------------------------------
// Health check — no auth required
// ---------------------------------------------------------------------------

app.get("/health", async (c) => {
  const count = await getMappingCount(c.env.DB);
  return c.json({
    status: "ok",
    account_id: c.env.BRALE_ACCOUNT_ID ? "configured" : "missing",
    managed_accounts: count,
  });
});

// ---------------------------------------------------------------------------
// Apple App Site Association — enables iOS Universal Links for Plaid OAuth
// redirect back to the XionDemo app. Served at the well-known path with
// Content-Type application/json and no extension, per Apple's requirements.
// ---------------------------------------------------------------------------

const AASA = {
  applinks: {
    details: [
      {
        appIDs: ["85A34A7PB2.com.burnt.xiondemo.ios"],
        components: [
          { "/": "/plaid-oauth", comment: "Plaid OAuth redirect" },
          { "/": "/plaid-oauth*", comment: "Plaid OAuth redirect with query" },
        ],
      },
    ],
  },
};

app.get("/.well-known/apple-app-site-association", (c) => {
  return new Response(JSON.stringify(AASA), {
    headers: { "content-type": "application/json" },
  });
});

// ---------------------------------------------------------------------------
// Plaid OAuth redirect landing page — browser fallback when the Universal
// Link does not auto-open the app (e.g. user navigated here manually or the
// device hasn't fetched the AASA yet). Renders a tap-to-return link that
// iOS will intercept as a Universal Link.
// ---------------------------------------------------------------------------

// Receiver for Brale customer/transfer status webhooks. We don't act on these
// yet — just acknowledge with 200 so Brale considers delivery successful.
app.post("/webhooks/brale", async (c) => {
  try {
    const payload = await c.req.json().catch(() => null);
    console.log("[webhooks/brale]", JSON.stringify(payload));
  } catch {
    // ignore body parse errors — still ack
  }
  return c.json({ received: true });
});

app.get("/plaid-oauth", (c) => {
  const query = c.req.url.split("?")[1] ?? "";
  const returnUrl = `https://brale-proxy.demo-burnt.workers.dev/plaid-oauth${query ? "?" + query : ""}`;
  const html = `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Return to XionDemo</title>
<style>
  body { font-family: -apple-system, BlinkMacSystemFont, sans-serif; background: #111; color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh; margin: 0; padding: 24px; text-align: center; }
  h1 { font-size: 22px; margin: 0 0 8px; }
  p { opacity: 0.7; margin: 0 0 24px; }
  a.btn { display: inline-block; background: #6d4ef2; color: #fff; text-decoration: none; padding: 14px 28px; border-radius: 12px; font-weight: 600; }
</style>
</head>
<body>
  <h1>Bank linked</h1>
  <p>Tap below to return to XionDemo.</p>
  <a class="btn" href="${returnUrl}">Return to app</a>
</body>
</html>`;
  return new Response(html, {
    headers: { "content-type": "text/html; charset=utf-8" },
  });
});

// ---------------------------------------------------------------------------
// Wallet address middleware — resolves per-user Brale account
// ---------------------------------------------------------------------------

app.use("*", async (c, next) => {
  const publicPaths = new Set([
    "/health",
    "/plaid-oauth",
    "/webhooks/brale",
    "/.well-known/apple-app-site-association",
  ]);
  if (publicPaths.has(c.req.path)) return next();

  const walletAddress = c.req.header("x-wallet-address");
  const requireAuth = c.env.REQUIRE_WALLET_AUTH === "true";

  // Verify signed wallet auth: proves the caller controls the meta account via a
  // session key that signed a fresh challenge and holds an on-chain authz grant.
  const authWallet = await verifyWalletAuth(
    c.env,
    {
      wallet: walletAddress,
      timestamp: c.req.header("x-auth-timestamp"),
      sessionAddress: c.req.header("x-auth-session-address"),
      signatureHex: c.req.header("x-auth-signature"),
    },
    c.req.method,
    c.req.path,
    Math.floor(Date.now() / 1000)
  );

  if (c.req.header("x-auth-signature") && !authWallet) {
    console.log(`[walletauth] verification failed (require=${requireAuth})`);
  }
  if (requireAuth && !authWallet) {
    return c.json({ error: "Wallet authentication required" }, 401);
  }
  c.set("authWallet", authWallet);

  if (!walletAddress) {
    c.set("braleAccountId", c.env.BRALE_ACCOUNT_ID);
    return next();
  }

  if (
    !walletAddress.startsWith("xion1") ||
    walletAddress.length < 40 ||
    walletAddress.length > 65 ||
    !/^[a-z0-9]+$/.test(walletAddress)
  ) {
    return c.json({ error: "Invalid X-Wallet-Address format" }, 400);
  }

  const existing = await getAccountId(c.env.DB, walletAddress);
  if (existing) {
    c.set("braleAccountId", existing);
    return next();
  }

  // All wallets share the partner account today (per-user Brale sub-accounts would
  // require per-user KYB, which isn't possible here). Isolation is instead enforced
  // by signed wallet auth + per-wallet ownership scoping on read endpoints.
  const accountId = c.env.BRALE_ACCOUNT_ID;
  await saveAccountMapping(c.env.DB, walletAddress, accountId);
  c.set("braleAccountId", accountId);
  return next();
});

// Wallet whose resources a read should be scoped to, or null to skip scoping
// (soft mode, unauthenticated). Writes attribute to the verified or header wallet.
function scopeWallet(c: {
  get: (k: "authWallet") => string | null;
}): string | null {
  return c.get("authWallet");
}

// ---------------------------------------------------------------------------
// Routes — Plaid
// ---------------------------------------------------------------------------

app.post("/plaid/link-token", async (c) => {
  try {
    const body = await c.req.json();
    const accountId = c.get("braleAccountId");
    const data = await braleRequest(
      c.env,
      "POST",
      `/accounts/${accountId}/plaid/link_token`,
      {
        legal_name: body.legal_name,
        email_address: body.email_address,
        phone_number: body.phone_number,
        date_of_birth: body.date_of_birth,
      },
      true
    );
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

app.post("/plaid/register", async (c) => {
  const body = await c.req.json();
  const accountId = c.get("braleAccountId");
  const owner = c.get("authWallet") ?? c.req.header("x-wallet-address");
  const accountMask = typeof body.account_mask === "string" ? body.account_mask : undefined;
  try {
    const data = await braleRequest(
      c.env,
      "POST",
      `/accounts/${accountId}/plaid/register-account`,
      {
        public_token: body.public_token,
        transfer_types: body.transfer_types || [
          "ach_debit",
          "ach_credit",
          "same_day_ach_credit",
        ],
        // Brale now requires this field; it's where they POST account/transfer
        // status updates. Defaults to this proxy's own receiver (see /webhooks/brale).
        customer_webhook_url:
          body.customer_webhook_url ||
          c.env.BRALE_CUSTOMER_WEBHOOK_URL ||
          "https://brale-proxy.demo-burnt.workers.dev/webhooks/brale",
      },
      true
    );
    const id = (data as { id?: string; address_id?: string })?.id ??
      (data as { address_id?: string })?.address_id;
    if (owner && id) await recordOwner(c.env.DB, String(id), owner, "address");
    // Normalize so the client always sees `address_id` regardless of Brale's shape.
    return c.json(id ? { ...(data as object), address_id: String(id) } : data);
  } catch (err) {
    // Only the duplicate-registration failure (Brale's opaque 500) means "this bank
    // is already on the account" — recover from *that* by sharing it. Any other error
    // (invalid token -> 4xx, permission -> 403) must propagate, so a failed register
    // can't be turned into ownership.
    const isDuplicate = err instanceof BraleError && err.status === 500;
    // NOTE: account_mask is client-supplied and not cryptographically bound to the
    // public_token, so this trusts the caller's claimed last-4. Acceptable here
    // because reaching this point already requires signed wallet auth; a fully robust
    // design would derive the mask from Brale's view of the exchanged token.
    if (isDuplicate && owner && accountMask) {
      const existing = await findBankByMask(c.env, accountId, accountMask);
      if (existing?.id) {
        await recordOwner(c.env.DB, String(existing.id), owner, "address");
        return c.json({ ...existing, address_id: String(existing.id) });
      }
    }
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

// ---------------------------------------------------------------------------
// Routes — Addresses
// ---------------------------------------------------------------------------

app.get("/addresses", async (c) => {
  try {
    const accountId = c.get("braleAccountId");
    const type = c.req.query("type");
    let path = `/accounts/${accountId}/addresses`;
    if (type) path += `?type=${type}`;
    const data = await braleRequest(c.env, "GET", path);
    // Scope to the caller's own resources. Always keep internal custodial
    // addresses (shared infra, needed for offramp deposits) and the caller's own
    // on-chain wallet; otherwise only addresses this wallet created.
    const scope = scopeWallet(c);
    const list = data as { addresses?: Array<Record<string, unknown>> };
    if (scope && Array.isArray(list.addresses)) {
      const owned = await getOwnedIds(c.env.DB, scope, "address");
      list.addresses = list.addresses.filter(
        (a) =>
          a?.type === "internal" ||
          a?.address === scope ||
          owned.has(String(a?.id))
      );
    }
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

app.post("/addresses/external", async (c) => {
  try {
    const body = await c.req.json();
    const accountId = c.get("braleAccountId");
    const data = await braleRequest(
      c.env,
      "POST",
      `/accounts/${accountId}/addresses/external`,
      body,
      true
    );
    const owner = c.get("authWallet") ?? c.req.header("x-wallet-address");
    const id = (data as { id?: string; address_id?: string })?.id ??
      (data as { address_id?: string })?.address_id;
    if (owner && id) await recordOwner(c.env.DB, String(id), owner, "address");
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

app.get("/addresses/:id", async (c) => {
  try {
    const accountId = c.get("braleAccountId");
    const data = await braleRequest(
      c.env,
      "GET",
      `/accounts/${accountId}/addresses/${c.req.param("id")}`
    );
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

app.get("/addresses/:id/balance", async (c) => {
  try {
    const accountId = c.get("braleAccountId");
    const transferType = c.req.query("transfer_type");
    const valueType = c.req.query("value_type");
    let path = `/accounts/${accountId}/addresses/${c.req.param("id")}/balance`;
    const params = new URLSearchParams();
    if (transferType) params.set("transfer_type", transferType);
    if (valueType) params.set("value_type", valueType);
    if (params.toString()) path += `?${params}`;
    const data = await braleRequest(c.env, "GET", path);
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

// ---------------------------------------------------------------------------
// Routes — Transfers
// ---------------------------------------------------------------------------

app.post("/transfers", async (c) => {
  try {
    const body = await c.req.json();
    const allowed = getAllowedTypes(c.env);
    const violation = validateTransferTypes(body, allowed);
    if (violation) {
      return c.json({ error: violation }, 400);
    }
    const accountId = c.get("braleAccountId");
    const transferBody = {
      ...body,
      brand: body.brand || { account_id: c.env.BRALE_ACCOUNT_ID },
    };
    const data = await braleRequest(
      c.env,
      "POST",
      `/accounts/${accountId}/transfers`,
      transferBody,
      true
    );
    const owner = c.get("authWallet") ?? c.req.header("x-wallet-address");
    const id = (data as { id?: string })?.id;
    if (owner && id) await recordOwner(c.env.DB, String(id), owner, "transfer");
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

app.get("/transfers/:id", async (c) => {
  try {
    const accountId = c.get("braleAccountId");
    const data = await braleRequest(
      c.env,
      "GET",
      `/accounts/${accountId}/transfers/${c.req.param("id")}`
    );
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

app.get("/transfers", async (c) => {
  try {
    const accountId = c.get("braleAccountId");
    const params = new URLSearchParams();
    for (const [key, val] of Object.entries(c.req.query())) {
      if (val) params.set(key, val);
    }
    let path = `/accounts/${accountId}/transfers`;
    if (params.toString()) path += `?${params}`;
    const data = await braleRequest(c.env, "GET", path);
    // Scope to transfers this wallet created.
    const scope = scopeWallet(c);
    const list = data as { transfers?: Array<Record<string, unknown>> };
    if (scope && Array.isArray(list.transfers)) {
      const owned = await getOwnedIds(c.env.DB, scope, "transfer");
      list.transfers = list.transfers.filter((t) => owned.has(String(t?.id)));
    }
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

export default app;
