import { secp256k1 } from "@noble/curves/secp256k1";
import { sha256 } from "@noble/hashes/sha256";
import { ripemd160 } from "@noble/hashes/ripemd160";
import { bech32 } from "@scure/base";
import type { Env } from "./types";

// How far the client timestamp may drift from the proxy clock (anti-replay).
const MAX_SKEW_SECONDS = 120;
// How long to trust a verified (meta-account, session-key) grant before re-checking on-chain.
const GRANT_CACHE_TTL_SECONDS = 300;

export interface WalletAuthHeaders {
  wallet?: string; // X-Wallet-Address (meta account)
  timestamp?: string; // X-Auth-Timestamp (unix seconds)
  sessionAddress?: string; // X-Auth-Session-Address (session key's xion address)
  signatureHex?: string; // X-Auth-Signature (64-byte r||s, hex)
}

function hexToBytes(hex: string): Uint8Array {
  const clean = hex.startsWith("0x") ? hex.slice(2) : hex;
  if (clean.length % 2 !== 0) throw new Error("odd-length hex");
  const out = new Uint8Array(clean.length / 2);
  for (let i = 0; i < out.length; i++) {
    out[i] = parseInt(clean.slice(i * 2, i * 2 + 2), 16);
  }
  return out;
}

// Cosmos/XION address = bech32(prefix, ripemd160(sha256(compressedPubkey))).
function pubkeyToXionAddress(pubkey: Uint8Array): string {
  const hash = ripemd160(sha256(pubkey));
  return bech32.encode("xion", bech32.toWords(hash));
}

// The session key signs with a non-recoverable 64-byte r||s signature, so recover
// candidate public keys and check whether any yields the claimed session address.
// A match proves the signature came from the holder of that address's private key.
function signatureMatchesAddress(
  signature: Uint8Array,
  msgHash: Uint8Array,
  claimedAddress: string
): boolean {
  let sig: ReturnType<typeof secp256k1.Signature.fromCompact>;
  try {
    sig = secp256k1.Signature.fromCompact(signature);
  } catch {
    return false;
  }
  for (const recBit of [0, 1]) {
    try {
      const point = sig.addRecoveryBit(recBit).recoverPublicKey(msgHash);
      if (pubkeyToXionAddress(point.toRawBytes(true)) === claimedAddress) {
        return true;
      }
    } catch {
      // try next recovery bit
    }
  }
  return false;
}

// The exact message the iOS session key signs. MUST match the client.
function challenge(wallet: string, timestamp: string): Uint8Array {
  return new TextEncoder().encode(`xiondemo-auth:${wallet}:${timestamp}`);
}

async function hasActiveGrant(
  env: Env,
  granter: string,
  grantee: string
): Promise<boolean> {
  const cacheKey = `grant:${granter}:${grantee}`;
  const cached = await env.TOKEN_CACHE.get(cacheKey);
  if (cached === "1") return true;

  const base = env.XION_REST_URL.endsWith("/")
    ? env.XION_REST_URL
    : env.XION_REST_URL + "/";
  const url = `${base}cosmos/authz/v1beta1/grants?granter=${granter}&grantee=${grantee}`;
  const res = await fetch(url);
  if (!res.ok) return false;
  const data = (await res.json()) as { grants?: unknown[] };
  const active = Array.isArray(data.grants) && data.grants.length > 0;
  if (active) {
    await env.TOKEN_CACHE.put(cacheKey, "1", {
      expirationTtl: GRANT_CACHE_TTL_SECONDS,
    });
  }
  return active;
}

/**
 * Verify that the caller controls the meta account named in X-Wallet-Address by
 * proving possession of a session key that (a) signed a fresh challenge and
 * (b) holds an active authz grant from that meta account on-chain.
 * Returns the verified wallet (meta account) address, or null if anything fails.
 */
export async function verifyWalletAuth(
  env: Env,
  h: WalletAuthHeaders,
  nowSeconds: number
): Promise<string | null> {
  if (!h.wallet || !h.timestamp || !h.sessionAddress || !h.signatureHex) {
    return null;
  }

  const ts = parseInt(h.timestamp, 10);
  if (!Number.isFinite(ts) || Math.abs(nowSeconds - ts) > MAX_SKEW_SECONDS) {
    return null;
  }

  let signature: Uint8Array;
  try {
    signature = hexToBytes(h.signatureHex);
  } catch {
    return null;
  }
  if (signature.length !== 64) return null;

  // signBytes hashes the message with SHA256 before signing. Recover the signer and
  // confirm it is the claimed session address.
  const msgHash = sha256(challenge(h.wallet, h.timestamp));
  if (!signatureMatchesAddress(signature, msgHash, h.sessionAddress)) return null;

  // The signature proves possession of the session key; the grant proves that key
  // is authorized by the claimed meta account. Both are required.
  const granted = await hasActiveGrant(env, h.wallet, h.sessionAddress);
  if (!granted) return null;

  return h.wallet;
}
