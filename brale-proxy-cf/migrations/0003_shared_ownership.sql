-- Allow a resource (bank/transfer) to be owned by MULTIPLE wallets, so a bank
-- account can be shared by several users who each proved access to it via Plaid.
-- SQLite can't alter a primary key in place, so rebuild the table with a
-- composite (resource_id, wallet_address) key, preserving existing rows.
ALTER TABLE resource_owner RENAME TO resource_owner_old;

CREATE TABLE resource_owner (
  resource_id TEXT NOT NULL,
  wallet_address TEXT NOT NULL,
  kind TEXT NOT NULL,
  PRIMARY KEY (resource_id, wallet_address)
);

INSERT OR IGNORE INTO resource_owner (resource_id, wallet_address, kind)
  SELECT resource_id, wallet_address, kind FROM resource_owner_old;

DROP TABLE resource_owner_old;

CREATE INDEX IF NOT EXISTS idx_resource_owner_wallet
  ON resource_owner (wallet_address, kind);
