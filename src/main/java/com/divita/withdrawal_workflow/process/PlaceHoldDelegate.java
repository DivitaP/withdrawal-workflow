package com.divita.withdrawal_workflow.process;

import com.divita.withdrawal_workflow.domain.HoldService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("placeHoldDelegate")
public class PlaceHoldDelegate implements JavaDelegate {

    private final HoldService holdService;

    public PlaceHoldDelegate(HoldService holdService) {
        this.holdService = holdService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String holdId = holdService.placeHold(
                execution.getProcessBusinessKey(),
                (String) execution.getVariable("accountId"),
                new BigDecimal(execution.getVariable("amount").toString()));
        execution.setVariable("holdId", holdId);
    }
}