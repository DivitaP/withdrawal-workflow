package com.divita.withdrawal_workflow.process;

import com.divita.withdrawal_workflow.domain.AccountService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("validateRequestDelegate")
public class ValidateRequestDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ValidateRequestDelegate.class);
    private static final Set<String> TYPES = Set.of("REGULAR", "HARDSHIP", "LOAN");

    private final AccountService accounts;

    public ValidateRequestDelegate(AccountService accounts) {
        this.accounts = accounts;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String accountId = (String) execution.getVariable("accountId");
        Double amount = (Double) execution.getVariable("amount");
        String type = (String) execution.getVariable("withdrawalType");

        String problem = null;
        if (!accounts.exists(accountId)) {
            problem = "Unknown account: " + accountId;
        } else if (amount == null || amount <= 0) {
            problem = "Amount must be greater than zero";
        } else if (type == null || !TYPES.contains(type.toUpperCase())) {
            problem = "Unsupported withdrawal type: " + type;
        }

        if (problem != null) {
            log.info("Request {} invalid: {}", execution.getProcessBusinessKey(), problem);
            execution.setVariable("rejectionReason", problem);
            throw new BpmnError("INVALID_REQUEST", problem);   // error code must match the BPMN error
        }
        execution.setVariable("withdrawalType", type.toUpperCase());
    }
}