import type { Env } from "./types";

export async function getAccountId(
  db: D1Database,
  walletAddress: string
): Promise<string | null> {
  const row = await db
    .prepare("SELECT brale_account_id FROM account_mappings WHERE wallet_address = ?")
    .bind(walletAddress)
    .first<{ brale_account_id: string }>();
  return row?.brale_account_id ?? null;
}

export async function saveAccountMapping(
  db: D1Database,
  walletAddress: string,
  braleAccountId: string
): Promise<void> {
  await db
    .prepare(
      "INSERT OR IGNORE INTO account_mappings (wallet_address, brale_account_id) VALUES (?, ?)"
    )
    .bind(walletAddress, braleAccountId)
    .run();
}

export async function getMappingCount(db: D1Database): Promise<number> {
  const row = await db
    .prepare("SELECT COUNT(*) as count FROM account_mappings")
    .first<{ count: number }>();
  return row?.count ?? 0;
}

// resource_owner maps a Brale address_id / transfer_id to the wallet that created
// it, so reads can be scoped per-wallet even though all users share one Brale account.

export async function recordOwner(
  db: D1Database,
  resourceId: string,
  walletAddress: string,
  kind: "address" | "transfer"
): Promise<void> {
  // Non-fatal: the Brale write already succeeded; a bookkeeping failure here must
  // not fail the response. Worst case the resource is unattributed until re-seen.
  try {
    await db
      .prepare(
        "INSERT OR IGNORE INTO resource_owner (resource_id, wallet_address, kind) VALUES (?, ?, ?)"
      )
      .bind(resourceId, walletAddress, kind)
      .run();
  } catch {
    // swallow
  }
}

export async function getOwnedIds(
  db: D1Database,
  walletAddress: string,
  kind: "address" | "transfer"
): Promise<Set<string>> {
  const { results } = await db
    .prepare(
      "SELECT resource_id FROM resource_owner WHERE wallet_address = ? AND kind = ?"
    )
    .bind(walletAddress, kind)
    .all<{ resource_id: string }>();
  return new Set((results ?? []).map((r) => r.resource_id));
}
