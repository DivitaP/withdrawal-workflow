package com.divita.withdrawal_workflow.domain;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>(Map.of(
            "ACC-1001", new BigDecimal("120000.00"),
            "ACC-1002", new BigDecimal("5000.00"),
            "RISK-2001", new BigDecimal("90000.00"),
            "FAIL-3001", new BigDecimal("80000.00")));

    public boolean exists(String accountId) {
        return accountId != null && balances.containsKey(accountId);
    }

    public BigDecimal balance(String accountId) {
        return balances.get(accountId);
    }
}