package com.divita.withdrawal_workflow.domain;

import org.springframework.stereotype.Service;

@Service
public class FraudService {
    /** 0 = safe, 100 = certain fraud. Deterministic so tests are predictable. */
    public int score(String accountId, double amount) {
        if (accountId.startsWith("RISK")) return 85;
        if (amount > 25_000) return 55;
        return 10;
    }
}