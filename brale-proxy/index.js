import "dotenv/config";
import express from "express";
import { v4 as uuidv4 } from "uuid";

const app = express();
app.use(express.json());

const {
  BRALE_CLIENT_ID,
  BRALE_CLIENT_SECRET,
  BRALE_ACCOUNT_ID,
  BRALE_API_URL = "https://api.brale.xyz",
  BRALE_AUTH_URL = "https://auth.brale.xyz",
  PORT = 3000,
  // Comma-separated allowlist of transfer types. Transfers with types not
  // in this list are rejected. Set to "*" to allow all (not recommended).
  ALLOWED_TRANSFER_TYPES = "xion_testnet,ach_debit,ach_credit,same_day_ach_credit,rtp_credit",
} = process.env;

const allowedTypes = new Set(
  ALLOWED_TRANSFER_TYPES === "*" ? [] : ALLOWED_TRANSFER_TYPES.split(",").map((t) => t.trim())
);
const enforceAllowlist = ALLOWED_TRANSFER_TYPES !== "*";

// ---------------------------------------------------------------------------
// Transfer type safety guard
// ---------------------------------------------------------------------------

function validateTransferTypes(body) {
  if (!enforceAllowlist) return null;
  for (const endpoint of [body?.source, body?.destination]) {
    if (endpoint?.transfer_type && !allowedTypes.has(endpoint.transfer_type)) {
      return `Transfer type "${endpoint.transfer_type}" is not allowed. Allowed: ${[...allowedTypes].join(", ")}`;
    }
  }
  return null;
}

// ---------------------------------------------------------------------------
// Token management — cache bearer token, refresh when expired
// ---------------------------------------------------------------------------

let cachedToken = null;
let tokenExpiresAt = 0;

async function getBearerToken() {
  if (cachedToken && Date.now() < tokenExpiresAt) return cachedToken;

  const credentials = Buffer.from(
    `${BRALE_CLIENT_ID}:${BRALE_CLIENT_SECRET}`
  ).toString("base64");

  const res = await fetch(`${BRALE_AUTH_URL}/oauth2/token`, {
    method: "POST",
    headers: {
      Authorization: `Basic ${credentials}`,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: "grant_type=client_credentials",
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Brale auth failed (${res.status}): ${text}`);
  }

  const data = await res.json();
  cachedToken = data.access_token;
  // Refresh 5 minutes before expiry
  tokenExpiresAt = Date.now() + (data.expires_in - 300) * 1000;
  return cachedToken;
}

// ---------------------------------------------------------------------------
// Brale API helper
// ---------------------------------------------------------------------------

async function braleRequest(method, path, body, idempotent = false) {
  const token = await getBearerToken();
  const headers = {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };
  if (idempotent) headers["Idempotency-Key"] = uuidv4();

  const url = `${BRALE_API_URL}${path}`;
  const options = { method, headers };
  if (body && method !== "GET") options.body = JSON.stringify(body);

  const res = await fetch(url, options);
  const text = await res.text();

  let data;
  try {
    data = JSON.parse(text);
  } catch {
    data = { raw: text };
  }

  if (!res.ok) {
    const error = new Error(`Brale API error (${res.status})`);
    error.status = res.status;
    error.data = data;
    throw error;
  }

  return data;
}

// ---------------------------------------------------------------------------
// Error handler
// ---------------------------------------------------------------------------

function handleError(res, err) {
  console.error(err.message || err);
  const status = err.status || 500;
  res.status(status).json({
    error: err.message || "Internal server error",
    details: err.data || null,
  });
}

// ---------------------------------------------------------------------------
// Routes — Plaid
// ---------------------------------------------------------------------------

// Create Plaid link token for bank account linking
app.post("/plaid/link-token", async (req, res) => {
  try {
    const { legal_name, email_address, phone_number, date_of_birth } = req.body;
    const data = await braleRequest(
      "POST",
      `/accounts/${BRALE_ACCOUNT_ID}/plaid/link_token`,
      { legal_name, email_address, phone_number, date_of_birth },
      true // idempotent
    );
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// Exchange Plaid public token to register bank account
app.post("/plaid/register", async (req, res) => {
  try {
    const { public_token, transfer_types } = req.body;
    const data = await braleRequest(
      "POST",
      `/accounts/${BRALE_ACCOUNT_ID}/plaid/register-account`,
      {
        public_token,
        transfer_types: transfer_types || [
          "ach_debit",
          "ach_credit",
          "same_day_ach_credit",
        ],
      },
      true // idempotent
    );
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// ---------------------------------------------------------------------------
// Routes — Addresses
// ---------------------------------------------------------------------------

// List addresses for the account
app.get("/addresses", async (req, res) => {
  try {
    const { type } = req.query;
    let path = `/accounts/${BRALE_ACCOUNT_ID}/addresses`;
    if (type) path += `?type=${type}`;
    const data = await braleRequest("GET", path);
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// Create external address (blockchain wallet or bank account)
app.post("/addresses/external", async (req, res) => {
  try {
    const data = await braleRequest(
      "POST",
      `/accounts/${BRALE_ACCOUNT_ID}/addresses/external`,
      req.body,
      true // idempotent
    );
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// Get single address
app.get("/addresses/:id", async (req, res) => {
  try {
    const data = await braleRequest(
      "GET",
      `/accounts/${BRALE_ACCOUNT_ID}/addresses/${req.params.id}`
    );
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// Get address balance
app.get("/addresses/:id/balance", async (req, res) => {
  try {
    const { transfer_type, value_type } = req.query;
    let path = `/accounts/${BRALE_ACCOUNT_ID}/addresses/${req.params.id}/balance`;
    const params = new URLSearchParams();
    if (transfer_type) params.set("transfer_type", transfer_type);
    if (value_type) params.set("value_type", value_type);
    if (params.toString()) path += `?${params}`;
    const data = await braleRequest("GET", path);
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// ---------------------------------------------------------------------------
// Routes — Transfers
// ---------------------------------------------------------------------------

// Create transfer (onramp or offramp)
app.post("/transfers", async (req, res) => {
  try {
    const violation = validateTransferTypes(req.body);
    if (violation) {
      return res.status(400).json({ error: violation });
    }
    // Inject brand (controls ACH bank statement name) server-side
    const body = {
      ...req.body,
      brand: req.body.brand || { account_id: BRALE_ACCOUNT_ID },
    };
    const data = await braleRequest(
      "POST",
      `/accounts/${BRALE_ACCOUNT_ID}/transfers`,
      body,
      true // idempotent
    );
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// Get transfer status
app.get("/transfers/:id", async (req, res) => {
  try {
    const data = await braleRequest(
      "GET",
      `/accounts/${BRALE_ACCOUNT_ID}/transfers/${req.params.id}`
    );
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// List transfers
app.get("/transfers", async (req, res) => {
  try {
    const params = new URLSearchParams();
    for (const [key, val] of Object.entries(req.query)) {
      params.set(key, val);
    }
    let path = `/accounts/${BRALE_ACCOUNT_ID}/transfers`;
    if (params.toString()) path += `?${params}`;
    const data = await braleRequest("GET", path);
    res.json(data);
  } catch (err) {
    handleError(res, err);
  }
});

// ---------------------------------------------------------------------------
// Health check
// ---------------------------------------------------------------------------

app.get("/health", (_req, res) => {
  res.json({ status: "ok", account_id: BRALE_ACCOUNT_ID ? "configured" : "missing" });
});

// ---------------------------------------------------------------------------
// Start
// ---------------------------------------------------------------------------

app.listen(PORT, () => {
  console.log(`Brale proxy running on port ${PORT}`);
  if (!BRALE_CLIENT_ID || !BRALE_CLIENT_SECRET || !BRALE_ACCOUNT_ID) {
    console.warn("WARNING: Missing Brale credentials in environment. Copy .env.example to .env and fill in values.");
  }
});
