package com.divita.withdrawal_workflow.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WithdrawalService {
    public static final String PROCESS_KEY = "withdrawal-request";

    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public WithdrawalService(RuntimeService runtimeService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    public StartResponse start(WithdrawalRequest req) {
        String requestId = (req.requestId() == null || req.requestId().isBlank())
                ? UUID.randomUUID().toString() : req.requestId();

        HistoricProcessInstance existing = find(requestId);
        if (existing != null) {
            return new StartResponse(requestId, existing.getId(), false);   // idempotent replay
        }

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                PROCESS_KEY,
                requestId,                                   // business key
                Variables.createVariables()
                        .putValue("requestId", requestId)
                        .putValue("accountId", req.accountId())
                        .putValue("amount", req.amount())
                        .putValue("withdrawalType", req.withdrawalType()));
        return new StartResponse(requestId, pi.getId(), true);
    }

    public Optional<WithdrawalStatus> status(String requestId) {
        HistoricProcessInstance hpi = find(requestId);
        if (hpi == null) return Optional.empty();

        boolean ended = hpi.getEndTime() != null;
        List<String> active = ended ? List.of() : runtimeService.getActiveActivityIds(hpi.getId());

        return Optional.of(new WithdrawalStatus(requestId, hpi.getId(), ended ? "ENDED": "RUNNING", active));
    }

    private HistoricProcessInstance find(String requestId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(PROCESS_KEY)
                .processInstanceBusinessKey(requestId)
                .singleResult();
    }
}
