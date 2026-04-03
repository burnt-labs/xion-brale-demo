use cosmwasm_std::{
    entry_point, to_json_binary, BankMsg, Binary, Coin, Deps, DepsMut, Env, Event, MessageInfo,
    Order, Response, StdResult,
};

use crate::error::ContractError;
use crate::msg::{
    BalanceResponse, ConfigResponse, ExecuteMsg, InstantiateMsg, QueryMsg, TotalDepositsResponse,
};
use crate::state::{Config, BALANCES, CONFIG};

#[entry_point]
pub fn instantiate(
    deps: DepsMut,
    _env: Env,
    _info: MessageInfo,
    msg: InstantiateMsg,
) -> Result<Response, ContractError> {
    let config = Config {
        allowed_denoms: msg.allowed_denoms,
    };
    CONFIG.save(deps.storage, &config)?;

    Ok(Response::new().add_attribute("action", "instantiate"))
}

#[entry_point]
pub fn execute(
    deps: DepsMut,
    _env: Env,
    info: MessageInfo,
    msg: ExecuteMsg,
) -> Result<Response, ContractError> {
    match msg {
        ExecuteMsg::Deposit {} => execute_deposit(deps, info),
        ExecuteMsg::Withdraw { coins } => execute_withdraw(deps, info, coins),
        ExecuteMsg::WithdrawAll {} => execute_withdraw_all(deps, info),
    }
}

fn execute_deposit(deps: DepsMut, info: MessageInfo) -> Result<Response, ContractError> {
    if info.funds.is_empty() {
        return Err(ContractError::NoFunds);
    }

    let config = CONFIG.load(deps.storage)?;

    // Validate all denoms are allowed
    for coin in &info.funds {
        if !config.allowed_denoms.contains(&coin.denom) {
            return Err(ContractError::DenomNotAllowed {
                denom: coin.denom.clone(),
            });
        }
    }

    let sender = info.sender.as_str();

    // Load existing balance or start empty
    let mut balance = BALANCES.may_load(deps.storage, sender)?.unwrap_or_default();

    // Add deposited funds to balance
    for deposit in &info.funds {
        if let Some(existing) = balance.iter_mut().find(|c| c.denom == deposit.denom) {
            existing.amount += deposit.amount;
        } else {
            balance.push(deposit.clone());
        }
    }

    BALANCES.save(deps.storage, sender, &balance)?;

    // Emit events for each deposited denom
    let mut event = Event::new("vault_deposit").add_attribute("user", info.sender.as_str());
    for coin in &info.funds {
        event = event
            .add_attribute("denom", &coin.denom)
            .add_attribute("amount", coin.amount.to_string());
    }

    Ok(Response::new().add_event(event))
}

fn execute_withdraw(
    deps: DepsMut,
    info: MessageInfo,
    coins: Vec<Coin>,
) -> Result<Response, ContractError> {
    let sender = info.sender.as_str();

    let mut balance = BALANCES
        .may_load(deps.storage, sender)?
        .ok_or(ContractError::NoBalance)?;

    // Deduct each requested coin
    for requested in &coins {
        let existing = balance
            .iter_mut()
            .find(|c| c.denom == requested.denom)
            .ok_or(ContractError::InsufficientBalance {
                denom: requested.denom.clone(),
                requested: requested.amount.to_string(),
                available: "0".to_string(),
            })?;

        if existing.amount < requested.amount {
            return Err(ContractError::InsufficientBalance {
                denom: requested.denom.clone(),
                requested: requested.amount.to_string(),
                available: existing.amount.to_string(),
            });
        }

        existing.amount -= requested.amount;
    }

    // Remove zero-balance entries
    balance.retain(|c| !c.amount.is_zero());

    // Save or remove the balance entry
    if balance.is_empty() {
        BALANCES.remove(deps.storage, sender);
    } else {
        BALANCES.save(deps.storage, sender, &balance)?;
    }

    // Send tokens back to the user
    let send_msg = BankMsg::Send {
        to_address: info.sender.to_string(),
        amount: coins.clone(),
    };

    let mut event = Event::new("vault_withdraw").add_attribute("user", info.sender.as_str());
    for coin in &coins {
        event = event
            .add_attribute("denom", &coin.denom)
            .add_attribute("amount", coin.amount.to_string());
    }

    Ok(Response::new().add_message(send_msg).add_event(event))
}

fn execute_withdraw_all(deps: DepsMut, info: MessageInfo) -> Result<Response, ContractError> {
    let sender = info.sender.as_str();

    let balance = BALANCES
        .may_load(deps.storage, sender)?
        .ok_or(ContractError::NoBalance)?;

    if balance.is_empty() {
        return Err(ContractError::NoBalance);
    }

    // Remove the balance entry
    BALANCES.remove(deps.storage, sender);

    // Send all tokens back to the user
    let send_msg = BankMsg::Send {
        to_address: info.sender.to_string(),
        amount: balance.clone(),
    };

    let mut event = Event::new("vault_withdraw").add_attribute("user", info.sender.as_str());
    for coin in &balance {
        event = event
            .add_attribute("denom", &coin.denom)
            .add_attribute("amount", coin.amount.to_string());
    }

    Ok(Response::new().add_message(send_msg).add_event(event))
}

#[entry_point]
pub fn query(deps: Deps, _env: Env, msg: QueryMsg) -> StdResult<Binary> {
    match msg {
        QueryMsg::Balance { address } => to_json_binary(&query_balance(deps, address)?),
        QueryMsg::Config {} => to_json_binary(&query_config(deps)?),
        QueryMsg::TotalDeposits {} => to_json_binary(&query_total_deposits(deps)?),
    }
}

fn query_balance(deps: Deps, address: String) -> StdResult<BalanceResponse> {
    let coins = BALANCES
        .may_load(deps.storage, &address)?
        .unwrap_or_default();
    Ok(BalanceResponse { coins })
}

fn query_config(deps: Deps) -> StdResult<ConfigResponse> {
    let config = CONFIG.load(deps.storage)?;
    Ok(ConfigResponse {
        allowed_denoms: config.allowed_denoms,
    })
}

fn query_total_deposits(deps: Deps) -> StdResult<TotalDepositsResponse> {
    let mut totals: Vec<Coin> = Vec::new();

    // Iterate over all balances
    let all_balances: Vec<(String, Vec<Coin>)> = BALANCES
        .range(deps.storage, None, None, Order::Ascending)
        .collect::<StdResult<Vec<_>>>()?;

    for (_addr, coins) in all_balances {
        for coin in coins {
            if let Some(existing) = totals.iter_mut().find(|c| c.denom == coin.denom) {
                existing.amount += coin.amount;
            } else {
                totals.push(coin);
            }
        }
    }

    Ok(TotalDepositsResponse { coins: totals })
}
