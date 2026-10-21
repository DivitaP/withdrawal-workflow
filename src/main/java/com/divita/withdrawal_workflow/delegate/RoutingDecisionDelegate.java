package com.divita.withdrawal_workflow.delegate;

import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("routingDecisionDelegate")
public class RoutingDecisionDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(RoutingDecisionDelegate.class);
    private static final String DECISION_KEY = "withdrawal-routing";

    private final DecisionService decisionService;

    public RoutingDecisionDelegate(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        VariableMap inputs = Variables.createVariables()
                .putValue("sufficientFunds", execution.getVariable("sufficientFunds"))
                .putValue("fraudScore", execution.getVariable("fraudScore"))
                .putValue("withdrawalType", execution.getVariable("withdrawalType"))
                .putValue("amount", execution.getVariable("amount"));

        inputs.forEach((k, v) -> log.info("DMN input {} = {} ({})", k, v,
                v == null ? "null" : v.getClass().getSimpleName()));

        DmnDecisionTableResult result =
                decisionService.evaluateDecisionTableByKey(DECISION_KEY, inputs);

        DmnDecisionRuleResult entry = result.getSingleResult();
        if (entry == null) {
            throw new IllegalStateException(
                    "No routing rule matched for requestId=" + execution.getBusinessKey());
        }

        String route = entry.getEntry("route");
        String reason = entry.getEntry("reason");

        execution.setVariable("route", route);
        execution.setVariable("rejectionReason", reason);

        log.info("Routing decision requestId={} route={} reason={}",
                execution.getBusinessKey(), route, reason);
    }
}