package com.divita.withdrawal_workflow.process;

import com.divita.withdrawal_workflow.domain.PayoutService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("payoutDelegate")
public class PayoutDelegate implements JavaDelegate {

    private final PayoutService payoutService;

    public PayoutDelegate(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String requestId = execution.getProcessBusinessKey();
        String accountId = (String) execution.getVariable("accountId");
        BigDecimal amount = new BigDecimal(execution.getVariable("amount").toString());

        String txnId = payoutService.pay(requestId, accountId, amount);

        if (payoutService.shouldCrashAfterPay()) {
            throw new RuntimeException("Simulated crash after payout");
        }

        execution.setVariable("payoutTxnId", txnId);
        execution.setVariable("outcome", "PAID");
    }
}