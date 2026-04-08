import type { Env } from "./types";

interface CachedToken {
  token: string;
  expiresAt: number;
}

export async function getBearerToken(env: Env): Promise<string> {
  const cached = await env.TOKEN_CACHE.get<CachedToken>("brale_token", {
    type: "json",
  });
  if (cached && Date.now() < cached.expiresAt) {
    return cached.token;
  }

  const credentials = btoa(`${env.BRALE_CLIENT_ID}:${env.BRALE_CLIENT_SECRET}`);

  const res = await fetch(`${env.BRALE_AUTH_URL}/oauth2/token`, {
    method: "POST",
    headers: {
      Authorization: `Basic ${credentials}`,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: "grant_type=client_credentials",
  });

  if (!res.ok) {
    const text = await res.text();
    throw new BraleError(`Brale auth failed (${res.status}): ${text}`, res.status);
  }

  const data = (await res.json()) as { access_token: string; expires_in: number };
  const expiresAt = Date.now() + (data.expires_in - 300) * 1000;

  await env.TOKEN_CACHE.put(
    "brale_token",
    JSON.stringify({ token: data.access_token, expiresAt }),
    { expirationTtl: Math.max(data.expires_in - 300, 60) }
  );

  return data.access_token;
}

export class BraleError extends Error {
  status: number;
  data: unknown;

  constructor(message: string, status: number = 500, data?: unknown) {
    super(message);
    this.status = status;
    this.data = data ?? null;
  }
}

export async function braleRequest(
  env: Env,
  method: string,
  path: string,
  body?: unknown,
  idempotent = false
): Promise<unknown> {
  const token = await getBearerToken(env);
  const headers: Record<string, string> = {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };
  if (idempotent) headers["Idempotency-Key"] = crypto.randomUUID();

  const url = `${env.BRALE_API_URL}${path}`;
  const options: RequestInit = { method, headers };
  if (body && method !== "GET") options.body = JSON.stringify(body);

  const res = await fetch(url, options);
  const text = await res.text();

  let data: unknown;
  try {
    data = JSON.parse(text);
  } catch {
    data = { raw: text };
  }

  if (!res.ok) {
    throw new BraleError(`Brale API error (${res.status})`, res.status, data);
  }

  return data;
}
