use cosmwasm_schema::{cw_serde, QueryResponses};
use cosmwasm_std::Coin;

#[cw_serde]
pub struct InstantiateMsg {
    /// Token denominations the vault accepts (e.g., ["uxion", "usbc"])
    pub allowed_denoms: Vec<String>,
    /// Admin address that can update allowed_denoms. Defaults to the instantiator if not set.
    pub admin: Option<String>,
}

#[cw_serde]
pub enum ExecuteMsg {
    /// Deposit native tokens into the vault. Tokens must be sent with the transaction.
    /// Only allowed_denoms are accepted. Credited to the sender's balance.
    Deposit {},

    /// Withdraw specific coins from the vault. Only the original depositor can withdraw.
    /// Withdrawals are always allowed regardless of current allowed_denoms.
    Withdraw { coins: Vec<Coin> },

    /// Withdraw all of the sender's balance from the vault.
    /// Withdrawals are always allowed regardless of current allowed_denoms.
    WithdrawAll {},

    /// Add or remove accepted token denominations. Only the admin can call this.
    /// Removing a denom does not affect existing deposits — users can still withdraw them.
    UpdateAllowedDenoms {
        add: Vec<String>,
        remove: Vec<String>,
    },
}

#[cw_serde]
#[derive(QueryResponses)]
pub enum QueryMsg {
    /// Returns the vault balance for a given address
    #[returns(BalanceResponse)]
    Balance { address: String },

    /// Returns the vault configuration
    #[returns(ConfigResponse)]
    Config {},

    /// Returns the total deposits across all users
    #[returns(TotalDepositsResponse)]
    TotalDeposits {},
}

#[cw_serde]
pub struct BalanceResponse {
    pub coins: Vec<Coin>,
}

#[cw_serde]
pub struct ConfigResponse {
    pub admin: String,
    pub allowed_denoms: Vec<String>,
}

#[cw_serde]
pub struct TotalDepositsResponse {
    pub coins: Vec<Coin>,
}
