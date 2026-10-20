package com.divita.withdrawal_workflow.api;

public record StartResponse(
        String requestId,
        String processInstanceId,
        boolean created
) {}
