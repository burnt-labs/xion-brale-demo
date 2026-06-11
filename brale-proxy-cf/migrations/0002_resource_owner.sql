-- Per-wallet ownership of Brale addresses/transfers, so reads can be scoped to the
-- caller's own resources even though all users share one Brale partner account.
CREATE TABLE IF NOT EXISTS resource_owner (
  resource_id TEXT PRIMARY KEY,
  wallet_address TEXT NOT NULL,
  kind TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_resource_owner_wallet
  ON resource_owner (wallet_address, kind);
