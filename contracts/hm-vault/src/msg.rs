use cosmwasm_schema::{cw_serde, QueryResponses};
use cosmwasm_std::Coin;

#[cw_serde]
pub struct InstantiateMsg {
    /// Token denominations the vault accepts (e.g., ["uxion", "usbc"])
    pub allowed_denoms: Vec<String>,
}

#[cw_serde]
pub enum ExecuteMsg {
    /// Deposit native tokens into the vault. Tokens must be sent with the transaction.
    /// Only allowed_denoms are accepted. Credited to the sender's balance.
    Deposit {},

    /// Withdraw specific coins from the vault. Only the original depositor can withdraw.
    Withdraw { coins: Vec<Coin> },

    /// Withdraw all of the sender's balance from the vault.
    WithdrawAll {},
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
    pub allowed_denoms: Vec<String>,
}

#[cw_serde]
pub struct TotalDepositsResponse {
    pub coins: Vec<Coin>,
}
