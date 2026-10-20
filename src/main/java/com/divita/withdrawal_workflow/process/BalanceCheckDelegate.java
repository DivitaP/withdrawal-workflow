package com.divita.withdrawal_workflow.process;

import com.divita.withdrawal_workflow.domain.AccountService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("balanceCheckDelegate")
public class BalanceCheckDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(BalanceCheckDelegate.class);
    private final AccountService accounts;

    public BalanceCheckDelegate(AccountService accounts) { this.accounts = accounts; }

    @Override
    public void execute(DelegateExecution execution) {
        BigDecimal balance = accounts.balance((String) execution.getVariable("accountId"));
        BigDecimal amount = BigDecimal.valueOf((Double) execution.getVariable("amount"));
        boolean sufficient = balance.compareTo(amount) >= 0;

        execution.setVariable("sufficientFunds", sufficient);           // a fact, not an exception
        execution.setVariable("availableBalance", balance.doubleValue()); // primitive, not BigDecimal
        log.info("[{}] balance check: sufficient={} (thread {})", execution.getProcessBusinessKey(),
                sufficient, Thread.currentThread().getName());
    }
}