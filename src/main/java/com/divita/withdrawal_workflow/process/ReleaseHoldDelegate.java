package com.divita.withdrawal_workflow.process;

import com.divita.withdrawal_workflow.domain.HoldService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("releaseHoldDelegate")
public class ReleaseHoldDelegate implements JavaDelegate {

    private final HoldService holdService;

    public ReleaseHoldDelegate(HoldService holdService) {
        this.holdService = holdService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        holdService.releaseHold(
                execution.getProcessBusinessKey(),
                (String) execution.getVariable("holdId"));
    }
}