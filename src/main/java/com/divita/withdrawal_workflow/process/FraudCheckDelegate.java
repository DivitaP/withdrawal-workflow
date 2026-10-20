package com.divita.withdrawal_workflow.process;

import com.divita.withdrawal_workflow.domain.FraudService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("fraudCheckDelegate")
public class FraudCheckDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(FraudCheckDelegate.class);
    private final FraudService fraud;

    public FraudCheckDelegate(FraudService fraud) { this.fraud = fraud; }

    @Override
    public void execute(DelegateExecution execution) {
        int score = fraud.score((String) execution.getVariable("accountId"),
                (Double) execution.getVariable("amount"));
        execution.setVariable("fraudScore", score);
        log.info("[{}] fraud score {} (thread {})", execution.getProcessBusinessKey(), score,
                Thread.currentThread().getName());
    }
}