package com.divita.withdrawal_workflow.process;

import com.divita.withdrawal_workflow.domain.HoldService;
import com.divita.withdrawal_workflow.domain.PayoutService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("payoutDelegate")
public class PayoutDelegate implements JavaDelegate {

    private final PayoutService payoutService;
    private final HoldService holdService;

    public PayoutDelegate(PayoutService payoutService, HoldService holdService) {
        this.payoutService = payoutService;
        this.holdService = holdService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String requestId = execution.getProcessBusinessKey();
        String accountId = (String) execution.getVariable("accountId");
        BigDecimal amount = new BigDecimal(execution.getVariable("amount").toString());

        String txnId = payoutService.pay(requestId, accountId, amount);

        Object holdId = execution.getVariable("holdId");
        if (holdId != null) {
            holdService.captureHold(requestId, (String) holdId);
        }

        if (payoutService.shouldCrashAfterPay()) {
            throw new RuntimeException("Simulated crash after payout");
        }

        execution.setVariable("payoutTxnId", txnId);
        execution.setVariable("outcome", "PAID");
    }
}