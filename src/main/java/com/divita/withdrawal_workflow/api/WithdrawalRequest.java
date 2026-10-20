package com.divita.withdrawal_workflow.api;

public record WithdrawalRequest(
        String requestId,
        String accountId,
        Double amount,
        String withdrawalType
) {}
