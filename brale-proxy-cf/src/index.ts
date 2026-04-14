import { Hono } from "hono";
import { cors } from "hono/cors";
import type { Env } from "./types";
import { braleRequest, BraleError } from "./auth";
import { getAccountId, saveAccountMapping, getMappingCount } from "./db";

type HonoEnv = { Bindings: Env; Variables: { braleAccountId: string } };

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
    "/.well-known/apple-app-site-association",
  ]);
  if (publicPaths.has(c.req.path)) return next();

  const walletAddress = c.req.header("x-wallet-address");

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

  // Stub: map all new wallets to the partner account
  // Replace with actual Brale managed account creation when API is available
  const accountId = c.env.BRALE_ACCOUNT_ID;
  await saveAccountMapping(c.env.DB, walletAddress, accountId);
  c.set("braleAccountId", accountId);
  return next();
});

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
  try {
    const body = await c.req.json();
    const accountId = c.get("braleAccountId");
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
      },
      true
    );
    return c.json(data);
  } catch (err) {
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
    return c.json(data);
  } catch (err) {
    const { error, details, _status } = errorResponse(err);
    return c.json({ error, details }, _status as 400);
  }
});

export default app;
