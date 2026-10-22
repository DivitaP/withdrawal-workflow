package com.divita.withdrawal_workflow.process;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("notificationService")
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void send(DelegateExecution execution, String outcome) {
        Object reason = execution.getVariable("rejectionReason");
        execution.setVariable("outcome", outcome);
        log.info("Notify customer [{}]: {} {}", execution.getProcessBusinessKey(), outcome,
                reason == null ? "" : "- " + reason);
    }

    public void escalate(DelegateExecution execution, String reason) {
        log.warn("Escalate review [{}]: {}", execution.getProcessBusinessKey(), reason);
    }
}