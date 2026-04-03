# Vault Contract Research & House Money Requirements

## What House Money Wants

The House Money team described a **hybrid custody model** with two account types per user:

### Checking Account (Custodial)
The everyday spending account. House Money has operational control — similar to how a bank controls your checking account:
- **Debit card spending** — when you swipe the H$ card, the system instantly debits this balance
- **Payroll deposits** land here
- **AI auto-allocation** — "put 10% of every paycheck into REITs" runs against this account
- **Reversals/disputes** — if someone disputes a payment, House Money can reverse it
- **Compliance controls** — AML screening, holds, freezes if required by law

### Vault (Non-Custodial)
A **savings safe** where the user — and *only* the user — controls funds. House Money cannot touch it:
- No debit card can pull from it
- No AI assistant can move money out of it
- No one can reverse a transaction from it
- House Money can't freeze it, even if the government asks (they technically *can't* — only the user's key works)

From the House Money document:
> *"Checking account: fully custodial, supports payments, payroll, cards, AI execution, and compliance controls."*
> *"Vault: optional non-custodial account for long-term storage; excluded from card spending, automated actions, and reversals."*

### Why a smart contract (not just a second wallet)?
- The **checking account** is a Meta Account on XION with authz grants that let House Money's backend, the AI assistant, and the card program execute transactions on the user's behalf
- The **vault** needs to be a *separate* on-chain entity with **no such grants** — a contract that enforces "only this user, only manual withdrawals"
- Provides clean, auditable separation of concerns on-chain
- Emits events for off-chain ledger reconciliation

---

## Full Product Vision (from House Money team)

### Core Customer Capabilities
- Send/receive money to any phone number, email, or H$ handle (U.S. and global, instant settlement)
- Wallet balance that functions as a neobank account
- Payroll ingestion and routing
- 24/7 voice and chat AI for property buying, wealth growth, lending, and income opportunities
- Automatic allocation of inbound funds based on user-defined strategies (e.g., 10% of payroll → REITs, T-bills, S&P, savings)
- Identity verification (KYC/KYB)
- H$-branded debit card with 1:1 relationship to wallet balance
- Monthly H$ lottery with cash prizes or free-home award

### Infrastructure Primitives Needed
- **Stable Monetary Base**: USD-denominated stable value unit, multi-chain compatible ("H$ dollar")
- **Ledger Architecture**: Off-chain payments ledger (authoritative) + on-chain settlement layer with deterministic reconciliation
- **Compliance & Identity**: KYC/KYB/AML workflows, sanctions screening
- **Hybrid Custody**: Abstracted user wallets, hot/warm/cold segregation, MPC or HSM-based custody
- **On/Off Ramps**: Payroll, debit card funding, instant debit card push, borderless payments
- **Crypto-Funded Card Program**: JIT funding at authorization, multi-chain redundancy

---

## Vault Contract Candidates Evaluated

### Candidate 1: cw-vault-standard (ApolloDAO)
- **Source**: https://github.com/apollodao/cw-vault-standard
- **Docs**: https://docs.rs/cw-vault-standard/0.4.1/cw_vault_standard/
- **Version**: 0.4.1 (August 2024), actively maintained

**What it is**: A standardized interface specification for tokenized vaults on CosmWasm. Defines `VaultStandardExecuteMsg` and `VaultStandardQueryMsg` enums.

**How it works**:
- Base Token: asset accepted for deposits (CW20 or native Cosmos token)
- Vault Token: share tokens representing proportional claim on a shared pool
- Extensions: Lockup, ForceUnlock, Keeper, Cw4626

**Custody model**: Pooled, contract-held custody. All user deposits go into a shared pool. Users receive share tokens as proof of claim. No per-user fund isolation.

**Designed for**: DeFi yield vaults (liquidity provision, lending, arbitrage), autocompounding strategies, pool-based capital deployment.

**Chains**: Osmosis (primary), Juno, Neutron, Archway

**Verdict: NOT a good fit.** The pooled share-token model is fundamentally wrong for a per-user non-custodial vault. Assumes funds are commingled and deployed into strategies.

---

### Candidate 2: Vaultenator (Margined Protocol)
- **Source**: https://github.com/margined-protocol/vaultenator
- **Latest activity**: August 2024, actively maintained

**What it is**: A framework/library for building CosmWasm vault contracts. Not a standalone vault — a reusable Rust crate. Implements CW4626 (ERC-4626 inspired) using TokenFactory for share tokens.

**How it works**:
- Trait-based architecture: `Vaultenator`, `Handle`, `ManageState`, `Describe`, `Query`, `Admin`, `Own`
- Built-in TokenFactory integration for issuing vault share tokens
- CosmWasm 2.1.3, primary target: Neutron

**Custody model**: Custodial by design — same pooled share-token model as cw-vault-standard. No per-user fund isolation.

**Verdict: NOT a good fit.** Same fundamental mismatch — designed for pooled DeFi yield strategies, not per-user non-custodial storage.

---

### Comparison Summary

| Aspect | cw-vault-standard | Vaultenator | House Money Vault Need |
|--------|-------------------|-------------|----------------------|
| **Type** | Interface spec | Implementation framework | Custom contract |
| **Model** | Pooled share tokens | Pooled share tokens (CW4626) | Per-user isolated custody |
| **Purpose** | DeFi yield strategies | DeFi yield framework | Long-term personal storage |
| **Custody** | Contract holds pooled funds | Contract holds pooled funds | User retains direct control |
| **Automation** | Built for it | Built for it | Explicitly excluded |
| **Fund isolation** | No (shared pool) | No (shared pool) | Required (per-user) |

### Recommendation
Neither contract is suitable. A custom vault contract (`hm-vault`) should be built — see `docs/superpowers/specs/2026-04-03-vault-contract-design.md` for the full design specification.
