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
