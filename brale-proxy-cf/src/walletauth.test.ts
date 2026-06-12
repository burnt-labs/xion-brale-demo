import { describe, it, expect } from "vitest";
import { secp256k1 } from "@noble/curves/secp256k1";
import { sha256 } from "@noble/hashes/sha256";
import { signatureMatchesAddress, pubkeyToXionAddress } from "./walletauth";

// Mirrors how the iOS session key signs (sha256(message) then low-S ECDSA, compact r||s)
// and how the proxy builds the request-bound challenge.
function sign(priv: Uint8Array, message: string): Uint8Array {
  const msgHash = sha256(new TextEncoder().encode(message));
  return secp256k1.sign(msgHash, priv, { lowS: true }).toCompactRawBytes();
}
function hashOf(message: string): Uint8Array {
  return sha256(new TextEncoder().encode(message));
}
function challenge(method: string, path: string, wallet: string, ts: string): string {
  return `xiondemo-auth:${method}:${path}:${wallet}:${ts}`;
}

const WALLET = "xion1w4ycz783jlvkshz8sne7hrpzj5d9g7uclr73mwdw72cq8c3cfags0puxze";
const TS = "1718200000";

describe("pubkeyToXionAddress", () => {
  it("derives a bech32 xion address from a compressed pubkey", () => {
    const priv = secp256k1.utils.randomPrivateKey();
    const pub = secp256k1.getPublicKey(priv, true);
    const addr = pubkeyToXionAddress(pub);
    expect(addr.startsWith("xion1")).toBe(true);
  });
});

describe("signatureMatchesAddress", () => {
  const priv = secp256k1.utils.randomPrivateKey();
  const sessionAddr = pubkeyToXionAddress(secp256k1.getPublicKey(priv, true));
  const msg = challenge("GET", "/addresses", WALLET, TS);

  it("accepts a valid signature for the claimed session address", () => {
    const sig = sign(priv, msg);
    expect(signatureMatchesAddress(sig, hashOf(msg), sessionAddr)).toBe(true);
  });

  it("rejects a signature against a different claimed address", () => {
    const sig = sign(priv, msg);
    const other = pubkeyToXionAddress(
      secp256k1.getPublicKey(secp256k1.utils.randomPrivateKey(), true)
    );
    expect(signatureMatchesAddress(sig, hashOf(msg), other)).toBe(false);
  });

  it("rejects when the message (request) differs — replay on another path fails", () => {
    const sig = sign(priv, msg); // signed for GET /addresses
    const tampered = challenge("POST", "/transfers", WALLET, TS);
    expect(signatureMatchesAddress(sig, hashOf(tampered), sessionAddr)).toBe(false);
  });

  it("rejects malformed signatures without throwing", () => {
    expect(signatureMatchesAddress(new Uint8Array(64), hashOf(msg), sessionAddr)).toBe(false);
  });
});
