# House Money Vault Contract — Design Specification

## Context

The House Money team needs a **non-custodial vault contract** on XION as part of their hybrid custody model. The system has two account types per user:

- **Checking Account** (custodial): House Money controls operations — card spending, payroll ingestion, AI auto-allocation, reversals, compliance holds. Implemented via XION Meta Accounts with authz grants to House Money's backend.
- **Vault** (non-custodial): User-only control for long-term storage. Excluded from card spending, automated actions, and reversals. **This is what we're building.**

Two existing CosmWasm vault contracts were evaluated and rejected:
- **cw-vault-standard** (ApolloDAO): Pooled DeFi yield vault interface — wrong model (shared pool, share tokens)
- **Vaultenator** (Margined Protocol): DeFi yield framework built on cw-vault-standard — same mismatch

Neither supports per-user fund isolation or the "no automation" requirement. A custom contract is needed.

## Architecture

### Approach: Single Per-User Vault Contract

One CosmWasm contract deployed once on XION. Maintains a per-user balance mapping. Only the depositor (identified by `info.sender`) can withdraw their own funds.

### Data Model

```rust
use cw_storage_plus::Map;
use cosmwasm_std::Coin;

/// Per-user balance: maps user address → list of coin balances
const BALANCES: Map<&Addr, Vec<Coin>> = Map::new("balances");

/// Contract configuration, set at instantiation
struct Config {
    /// Token denominations the vault accepts (e.g., ["uxion", "usbc"])
    allowed_denoms: Vec<String>,
}

const CONFIG: Item<Config> = Item::new("config");
```

### Execute Messages

| Message | Caller | Behavior |
|---------|--------|----------|
| `Deposit {}` | Any user | Accepts native tokens sent with the tx (`info.funds`). Credits each coin to the sender's balance. Rejects if any denom is not in `allowed_denoms`. |
| `Withdraw { coins: Vec<Coin> }` | Depositor only | Deducts specified coins from caller's balance and sends them back via `BankMsg::Send`. Fails if insufficient balance. |
| `WithdrawAll {}` | Depositor only | Sends all of the caller's vault balance back to them. Removes their entry from the map. |

### Query Messages

| Message | Returns |
|---------|---------|
| `QueryBalance { address: String }` | `BalanceResponse { coins: Vec<Coin> }` |
| `QueryConfig {}` | `ConfigResponse { allowed_denoms: Vec<String> }` |
| `QueryTotalDeposits {}` | `TotalDepositsResponse { coins: Vec<Coin> }` |

### What the contract does NOT have

- **No admin/owner role** that can move or freeze user funds
- **No `ForceWithdraw`** or `Freeze` message
- **No automation hooks** or callback interfaces
- **No share tokens** or pooling — balances are per-user, not proportional claims
- **No `UpdateConfig`** — `allowed_denoms` is set at instantiation and cannot be changed
- **No upgrade admin** — migrate authority can be set to XION governance or burned at deployment

### Events

```
wasm-vault_deposit { user: "xion1...", denom: "usbc", amount: "50000000" }
wasm-vault_withdraw { user: "xion1...", denom: "usbc", amount: "25000000" }
```

Events enable off-chain indexers to reconcile vault state with the House Money ledger.

## Integration

### Flow: Deposit to Vault

```
1. User taps "Move to Vault" in mobile app
2. App builds MsgExecuteContract:
   - contract: hm-vault address
   - msg: { "deposit": {} }
   - funds: [{ denom: "usbc", amount: "50000000" }]
3. Signed via session key (authz grant from Meta Account)
4. Contract credits BALANCES[sender] += funds
5. Emits vault_deposit event
6. Off-chain indexer updates ledger
```

### Flow: Withdraw from Vault

```
1. User taps "Move to Checking" in mobile app
2. App builds MsgExecuteContract:
   - contract: hm-vault address
   - msg: { "withdraw": { "coins": [{ "denom": "usbc", "amount": "25000000" }] } }
3. Signed via session key
4. Contract deducts from BALANCES[sender], sends via BankMsg::Send
5. Emits vault_withdraw event
6. Off-chain indexer updates ledger
```

### Flow: Query Balance

```
1. App sends QueryMsg to contract via REST:
   GET /cosmwasm/wasm/v1/contract/{vault_addr}/smart/{base64_query}
   query: { "query_balance": { "address": "xion1user..." } }
2. Returns: { "coins": [{ "denom": "usbc", "amount": "50000000" }] }
3. Displayed in wallet UI alongside checking balance
```

### Security Property

Even if House Money's entire backend is compromised, an attacker **cannot withdraw from the vault**. The contract enforces that only `info.sender` (the original depositor) can call `Withdraw`. This is enforced at the XION consensus layer — no API key, OAuth token, or backend credential can bypass it.

## Token Support

- **uxion** — native XION token
- **SBC stablecoin** — Brale-minted stablecoin (the H$ stable unit on-chain)

The `allowed_denoms` config rejects deposits of unknown tokens, preventing dust attacks.

## Critical Files

| File | Purpose |
|------|---------|
| `contracts/hm-vault/src/contract.rs` | Contract entry points (instantiate, execute, query) |
| `contracts/hm-vault/src/state.rs` | State definitions (BALANCES, CONFIG) |
| `contracts/hm-vault/src/msg.rs` | Message types (InstantiateMsg, ExecuteMsg, QueryMsg) |
| `contracts/hm-vault/src/error.rs` | Custom error types |
| `contracts/hm-vault/Cargo.toml` | Dependencies |
| `contracts/hm-vault/src/tests.rs` | Unit tests |

## Verification Plan

1. **Unit tests** (cw-multi-test):
   - Deposit native tokens → balance increases
   - Withdraw partial → balance decreases, user receives tokens
   - WithdrawAll → balance zeroed, user receives all tokens
   - Unauthorized withdrawal → rejected (different sender)
   - Deposit unsupported denom → rejected
   - Withdraw more than balance → rejected
   - Multiple users with independent balances
   - Multiple denominations per user

2. **Integration test** (xion-testnet-2):
   - Deploy contract via `wasmd` or mob library
   - Deposit from a Meta Account
   - Query balance via REST
   - Withdraw back to Meta Account
   - Verify events in transaction logs

3. **Security audit checklist**:
   - No message path allows non-depositor withdrawal
   - No admin function can move user funds
   - No re-entrancy vectors (CosmWasm is safe by default but verify)
   - Integer overflow protection on balance operations
   - Empty balance cleanup (prevent state bloat)

4. **Mobile integration**:
   - Deposit from iOS/Android app via mob
   - Display vault balance in wallet screen
   - Withdraw back to checking
