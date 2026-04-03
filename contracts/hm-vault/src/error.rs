use cosmwasm_std::StdError;
use thiserror::Error;

#[derive(Error, Debug, PartialEq)]
pub enum ContractError {
    #[error("{0}")]
    Std(#[from] StdError),

    #[error("No funds sent with deposit")]
    NoFunds,

    #[error("Denom {denom} is not accepted by this vault")]
    DenomNotAllowed { denom: String },

    #[error("Insufficient balance: requested {requested} {denom}, available {available}")]
    InsufficientBalance {
        denom: String,
        requested: String,
        available: String,
    },

    #[error("No balance to withdraw")]
    NoBalance,
}
