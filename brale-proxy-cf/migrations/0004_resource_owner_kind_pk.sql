-- Include `kind` in the primary key. Address IDs and transfer IDs come from
-- separate Brale sequences and could in principle collide; without `kind` in the
-- key, an address and a transfer sharing a resource_id + wallet would conflict and
-- one ownership row would be silently dropped, breaking that wallet's scoping.
ALTER TABLE resource_owner RENAME TO resource_owner_old;

CREATE TABLE resource_owner (
  resource_id TEXT NOT NULL,
  wallet_address TEXT NOT NULL,
  kind TEXT NOT NULL,
  PRIMARY KEY (resource_id, wallet_address, kind)
);

INSERT OR IGNORE INTO resource_owner (resource_id, wallet_address, kind)
  SELECT resource_id, wallet_address, kind FROM resource_owner_old;

DROP TABLE resource_owner_old;

CREATE INDEX IF NOT EXISTS idx_resource_owner_wallet
  ON resource_owner (wallet_address, kind);
