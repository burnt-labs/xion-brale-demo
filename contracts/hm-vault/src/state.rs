use cosmwasm_schema::cw_serde;
use cosmwasm_std::{Addr, Coin};
use cw_storage_plus::{Item, Map};

#[cw_serde]
pub struct Config {
    pub admin: Addr,
    pub allowed_denoms: Vec<String>,
}

pub const CONFIG: Item<Config> = Item::new("config");

/// Per-user balance: maps user address string → list of coin balances
pub const BALANCES: Map<&str, Vec<Coin>> = Map::new("balances");
