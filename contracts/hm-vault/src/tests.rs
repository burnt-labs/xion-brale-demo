use cosmwasm_std::{coins, Addr, Coin, Uint128};
use cw_multi_test::{App, ContractWrapper, Executor};

use crate::contract::{execute, instantiate, query};
use crate::msg::{
    BalanceResponse, ConfigResponse, ExecuteMsg, InstantiateMsg, QueryMsg, TotalDepositsResponse,
};

fn setup_app() -> (App, Addr, Addr) {
    let mut app = App::default();

    let code = ContractWrapper::new(execute, instantiate, query);
    let code_id = app.store_code(Box::new(code));

    let creator = app.api().addr_make("creator");
    let contract_addr = app
        .instantiate_contract(
            code_id,
            creator.clone(),
            &InstantiateMsg {
                allowed_denoms: vec!["uxion".to_string(), "usbc".to_string()],
                admin: None,
            },
            &[],
            "hm-vault",
            None,
        )
        .unwrap();

    (app, contract_addr, creator)
}

#[test]
fn test_instantiate() {
    let (app, contract_addr, creator) = setup_app();

    let res: ConfigResponse = app
        .wrap()
        .query_wasm_smart(contract_addr, &QueryMsg::Config {})
        .unwrap();

    assert_eq!(
        res.allowed_denoms,
        vec!["uxion".to_string(), "usbc".to_string()]
    );
    assert_eq!(res.admin, creator.to_string());
}

#[test]
fn test_deposit() {
    let (mut app, contract_addr, _) = setup_app();

    let user = app.api().addr_make("user1");

    // Fund the user
    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user, coins(1_000_000, "uxion"))
            .unwrap();
    });

    // Deposit
    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(500_000, "uxion"),
    )
    .unwrap();

    // Query balance
    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: user.to_string(),
            },
        )
        .unwrap();

    assert_eq!(res.coins, coins(500_000, "uxion"));
}

#[test]
fn test_deposit_multiple_times() {
    let (mut app, contract_addr, _) = setup_app();
    let user = app.api().addr_make("user1");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user, coins(1_000_000, "uxion"))
            .unwrap();
    });

    // First deposit
    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(300_000, "uxion"),
    )
    .unwrap();

    // Second deposit
    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(200_000, "uxion"),
    )
    .unwrap();

    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: user.to_string(),
            },
        )
        .unwrap();

    assert_eq!(res.coins, coins(500_000, "uxion"));
}

#[test]
fn test_deposit_no_funds() {
    let (mut app, contract_addr, _) = setup_app();
    let user = app.api().addr_make("user1");

    let err = app
        .execute_contract(user, contract_addr, &ExecuteMsg::Deposit {}, &[])
        .unwrap_err();

    assert!(err.root_cause().to_string().contains("No funds sent"));
}

#[test]
fn test_deposit_disallowed_denom() {
    let (mut app, contract_addr, _) = setup_app();
    let user = app.api().addr_make("user1");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user, coins(1_000_000, "uatom"))
            .unwrap();
    });

    let err = app
        .execute_contract(
            user,
            contract_addr,
            &ExecuteMsg::Deposit {},
            &coins(500_000, "uatom"),
        )
        .unwrap_err();

    assert!(err
        .root_cause()
        .to_string()
        .contains("not accepted by this vault"));
}

#[test]
fn test_withdraw_partial() {
    let (mut app, contract_addr, _) = setup_app();
    let user = app.api().addr_make("user1");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user, coins(1_000_000, "uxion"))
            .unwrap();
    });

    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(500_000, "uxion"),
    )
    .unwrap();

    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Withdraw {
            coins: coins(200_000, "uxion"),
        },
        &[],
    )
    .unwrap();

    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: user.to_string(),
            },
        )
        .unwrap();

    assert_eq!(res.coins, coins(300_000, "uxion"));

    let user_balance = app.wrap().query_balance(&user, "uxion").unwrap();
    assert_eq!(user_balance.amount, Uint128::new(700_000));
}

#[test]
fn test_withdraw_all() {
    let (mut app, contract_addr, _) = setup_app();
    let user = app.api().addr_make("user1");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user, coins(1_000_000, "uxion"))
            .unwrap();
    });

    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(500_000, "uxion"),
    )
    .unwrap();

    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::WithdrawAll {},
        &[],
    )
    .unwrap();

    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: user.to_string(),
            },
        )
        .unwrap();

    assert!(res.coins.is_empty());

    let user_balance = app.wrap().query_balance(&user, "uxion").unwrap();
    assert_eq!(user_balance.amount, Uint128::new(1_000_000));
}

#[test]
fn test_unauthorized_withdraw() {
    let (mut app, contract_addr, _) = setup_app();
    let user1 = app.api().addr_make("user1");
    let user2 = app.api().addr_make("user2");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user1, coins(1_000_000, "uxion"))
            .unwrap();
    });

    app.execute_contract(
        user1.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(500_000, "uxion"),
    )
    .unwrap();

    let err = app
        .execute_contract(
            user2,
            contract_addr.clone(),
            &ExecuteMsg::Withdraw {
                coins: coins(500_000, "uxion"),
            },
            &[],
        )
        .unwrap_err();

    assert!(err.root_cause().to_string().contains("No balance"));

    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: user1.to_string(),
            },
        )
        .unwrap();

    assert_eq!(res.coins, coins(500_000, "uxion"));
}

#[test]
fn test_withdraw_insufficient_balance() {
    let (mut app, contract_addr, _) = setup_app();
    let user = app.api().addr_make("user1");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user, coins(1_000_000, "uxion"))
            .unwrap();
    });

    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(500_000, "uxion"),
    )
    .unwrap();

    let err = app
        .execute_contract(
            user,
            contract_addr,
            &ExecuteMsg::Withdraw {
                coins: coins(999_999, "uxion"),
            },
            &[],
        )
        .unwrap_err();

    assert!(err.root_cause().to_string().contains("Insufficient balance"));
}

#[test]
fn test_multi_user_isolation() {
    let (mut app, contract_addr, _) = setup_app();
    let user1 = app.api().addr_make("user1");
    let user2 = app.api().addr_make("user2");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user1, coins(1_000_000, "uxion"))
            .unwrap();
        router
            .bank
            .init_balance(storage, &user2, coins(2_000_000, "uxion"))
            .unwrap();
    });

    app.execute_contract(
        user1.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(500_000, "uxion"),
    )
    .unwrap();

    app.execute_contract(
        user2.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(1_000_000, "uxion"),
    )
    .unwrap();

    let res1: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr.clone(),
            &QueryMsg::Balance {
                address: user1.to_string(),
            },
        )
        .unwrap();
    assert_eq!(res1.coins, coins(500_000, "uxion"));

    let res2: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr.clone(),
            &QueryMsg::Balance {
                address: user2.to_string(),
            },
        )
        .unwrap();
    assert_eq!(res2.coins, coins(1_000_000, "uxion"));

    app.execute_contract(
        user1,
        contract_addr.clone(),
        &ExecuteMsg::WithdrawAll {},
        &[],
    )
    .unwrap();

    let res2_after: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: user2.to_string(),
            },
        )
        .unwrap();
    assert_eq!(res2_after.coins, coins(1_000_000, "uxion"));
}

#[test]
fn test_multi_denom() {
    let (mut app, contract_addr, _) = setup_app();
    let user = app.api().addr_make("user1");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(
                storage,
                &user,
                vec![
                    Coin::new(1_000_000u128, "uxion"),
                    Coin::new(2_000_000u128, "usbc"),
                ],
            )
            .unwrap();
    });

    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &[
            Coin::new(500_000u128, "usbc"),
            Coin::new(300_000u128, "uxion"),
        ],
    )
    .unwrap();

    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr.clone(),
            &QueryMsg::Balance {
                address: user.to_string(),
            },
        )
        .unwrap();

    assert_eq!(res.coins.len(), 2);
    let uxion = res.coins.iter().find(|c| c.denom == "uxion").unwrap();
    let usbc = res.coins.iter().find(|c| c.denom == "usbc").unwrap();
    assert_eq!(uxion.amount, Uint128::new(300_000));
    assert_eq!(usbc.amount, Uint128::new(500_000));

    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Withdraw {
            coins: coins(100_000, "uxion"),
        },
        &[],
    )
    .unwrap();

    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: user.to_string(),
            },
        )
        .unwrap();

    let uxion = res.coins.iter().find(|c| c.denom == "uxion").unwrap();
    let usbc = res.coins.iter().find(|c| c.denom == "usbc").unwrap();
    assert_eq!(uxion.amount, Uint128::new(200_000));
    assert_eq!(usbc.amount, Uint128::new(500_000));
}

#[test]
fn test_total_deposits() {
    let (mut app, contract_addr, _) = setup_app();
    let user1 = app.api().addr_make("user1");
    let user2 = app.api().addr_make("user2");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user1, coins(1_000_000, "uxion"))
            .unwrap();
        router
            .bank
            .init_balance(storage, &user2, coins(2_000_000, "uxion"))
            .unwrap();
    });

    app.execute_contract(
        user1,
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(500_000, "uxion"),
    )
    .unwrap();

    app.execute_contract(
        user2,
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(800_000, "uxion"),
    )
    .unwrap();

    let res: TotalDepositsResponse = app
        .wrap()
        .query_wasm_smart(contract_addr, &QueryMsg::TotalDeposits {})
        .unwrap();

    assert_eq!(res.coins, coins(1_300_000, "uxion"));
}

#[test]
fn test_query_nonexistent_balance() {
    let (app, contract_addr, _) = setup_app();

    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: "xion1nobody".to_string(),
            },
        )
        .unwrap();

    assert!(res.coins.is_empty());
}

// --- Admin tests ---

#[test]
fn test_update_allowed_denoms_by_admin() {
    let (mut app, contract_addr, admin) = setup_app();

    // Add "uatom", remove "usbc"
    app.execute_contract(
        admin,
        contract_addr.clone(),
        &ExecuteMsg::UpdateAllowedDenoms {
            add: vec!["uatom".to_string()],
            remove: vec!["usbc".to_string()],
        },
        &[],
    )
    .unwrap();

    let res: ConfigResponse = app
        .wrap()
        .query_wasm_smart(contract_addr, &QueryMsg::Config {})
        .unwrap();

    assert!(res.allowed_denoms.contains(&"uxion".to_string()));
    assert!(res.allowed_denoms.contains(&"uatom".to_string()));
    assert!(!res.allowed_denoms.contains(&"usbc".to_string()));
}

#[test]
fn test_update_allowed_denoms_unauthorized() {
    let (mut app, contract_addr, _) = setup_app();
    let random_user = app.api().addr_make("random");

    let err = app
        .execute_contract(
            random_user,
            contract_addr,
            &ExecuteMsg::UpdateAllowedDenoms {
                add: vec!["uatom".to_string()],
                remove: vec![],
            },
            &[],
        )
        .unwrap_err();

    assert!(err.root_cause().to_string().contains("Unauthorized"));
}

#[test]
fn test_withdraw_after_denom_removed() {
    let (mut app, contract_addr, admin) = setup_app();
    let user = app.api().addr_make("user1");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user, coins(1_000_000, "usbc"))
            .unwrap();
    });

    // Deposit usbc while it's still allowed
    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Deposit {},
        &coins(500_000, "usbc"),
    )
    .unwrap();

    // Admin removes usbc from allowed denoms
    app.execute_contract(
        admin,
        contract_addr.clone(),
        &ExecuteMsg::UpdateAllowedDenoms {
            add: vec![],
            remove: vec!["usbc".to_string()],
        },
        &[],
    )
    .unwrap();

    // User can still withdraw their usbc
    app.execute_contract(
        user.clone(),
        contract_addr.clone(),
        &ExecuteMsg::Withdraw {
            coins: coins(500_000, "usbc"),
        },
        &[],
    )
    .unwrap();

    let res: BalanceResponse = app
        .wrap()
        .query_wasm_smart(
            contract_addr,
            &QueryMsg::Balance {
                address: user.to_string(),
            },
        )
        .unwrap();

    assert!(res.coins.is_empty());

    let user_balance = app.wrap().query_balance(&user, "usbc").unwrap();
    assert_eq!(user_balance.amount, Uint128::new(1_000_000));
}

#[test]
fn test_deposit_after_denom_removed() {
    let (mut app, contract_addr, admin) = setup_app();
    let user = app.api().addr_make("user1");

    app.init_modules(|router, _, storage| {
        router
            .bank
            .init_balance(storage, &user, coins(1_000_000, "usbc"))
            .unwrap();
    });

    // Admin removes usbc
    app.execute_contract(
        admin,
        contract_addr.clone(),
        &ExecuteMsg::UpdateAllowedDenoms {
            add: vec![],
            remove: vec!["usbc".to_string()],
        },
        &[],
    )
    .unwrap();

    // New deposits of usbc are rejected
    let err = app
        .execute_contract(
            user,
            contract_addr,
            &ExecuteMsg::Deposit {},
            &coins(500_000, "usbc"),
        )
        .unwrap_err();

    assert!(err
        .root_cause()
        .to_string()
        .contains("not accepted by this vault"));
}
