package com.divita.withdrawal_workflow.api;

import java.util.List;

public record WithdrawalStatus(
        String requestId,
        String processInstanceId,
        String state,
        List<String> currentActivities
) {}
