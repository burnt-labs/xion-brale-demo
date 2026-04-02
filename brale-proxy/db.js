import Database from "better-sqlite3";
import { mkdirSync, existsSync } from "fs";
import { dirname } from "path";

const DB_PATH = process.env.DB_PATH || "./data/accounts.db";

// Ensure directory exists
const dir = dirname(DB_PATH);
if (!existsSync(dir)) mkdirSync(dir, { recursive: true });

const db = new Database(DB_PATH);
db.pragma("journal_mode = WAL");

db.exec(`
  CREATE TABLE IF NOT EXISTS account_mappings (
    wallet_address TEXT PRIMARY KEY,
    brale_account_id TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
  )
`);

const stmtGet = db.prepare("SELECT brale_account_id FROM account_mappings WHERE wallet_address = ?");
const stmtInsert = db.prepare("INSERT OR IGNORE INTO account_mappings (wallet_address, brale_account_id) VALUES (?, ?)");
const stmtCount = db.prepare("SELECT COUNT(*) as count FROM account_mappings");

export function getAccountId(walletAddress) {
  const row = stmtGet.get(walletAddress);
  return row ? row.brale_account_id : null;
}

export function saveAccountMapping(walletAddress, braleAccountId) {
  stmtInsert.run(walletAddress, braleAccountId);
}

export function getMappingCount() {
  return stmtCount.get().count;
}

export function close() {
  db.close();
}
