package com.divita.withdrawal_workflow.process;

import com.divita.withdrawal_workflow.domain.HoldService;
import com.divita.withdrawal_workflow.domain.PayoutService;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
class WithdrawalProcessTest {

    private static final String PROCESS_KEY = "withdrawal-request"; // your process ID from the Modeler

    @Autowired ProcessEngine processEngine;
    @Autowired RuntimeService runtimeService;
    @Autowired ManagementService managementService;
    @Autowired PayoutService payoutService;
    @MockitoSpyBean HoldService holdService;

    @BeforeEach
    void setUp() {
        init(processEngine);
        payoutService.setGatewayUp(false);
    }

    private ProcessInstance start(String requestId, String accountId, double amount, String type) {
        return runtimeService.startProcessInstanceByKey(PROCESS_KEY, requestId,
                Variables.createVariables()
                        .putValue("requestId", requestId)
                        .putValue("accountId", accountId)
                        .putValue("amount", amount)
                        .putValue("withdrawalType", type));
    }

    @Test
    void invalidRequest_isRejectedBeforeChecks() {
        ProcessInstance pi = start("it-invalid", "UNKNOWN-1", 500, "REGULAR");

        assertThat(pi).isEnded().hasPassed("endRejected");
        Assertions.assertThat((String) historyVar(pi, "rejectionReason")).contains("Unknown account");
    }

    @Test
    void autoApprove_paysOutAsynchronously() {
        ProcessInstance pi = start("it-auto", "ACC-1001", 500, "REGULAR");

        assertThat(pi).isWaitingAt("executePayout");   // asyncBefore created a job
        execute(job());

        assertThat(pi).isEnded().hasPassed("endPaid").hasNotPassed("placeHold");
        Assertions.assertThat((String) historyVar(pi, "route")).isEqualTo("AUTO_APPROVE");
        Assertions.assertThat((String) historyVar(pi, "payoutTxnId")).startsWith("TXN-");
    }

    @Test
    void dmnReject_insufficientFunds() {
        ProcessInstance pi = start("it-nofunds", "ACC-1002", 9000, "REGULAR");

        assertThat(pi).isEnded().hasPassed("endRejected").hasNotPassed("executePayout");
        Assertions.assertThat((String) historyVar(pi, "rejectionReason")).isEqualTo("Insufficient funds");
    }

    @Test
    void manualReview_approved_capturesHoldAndPays() {
        ProcessInstance pi = start("it-review-ok", "ACC-1001", 15000, "REGULAR");

        assertThat(pi).isWaitingAt("reviewWithdrawal").hasPassed("placeHold");
        complete(task(pi), withVariables("approved", true));

        assertThat(pi).isWaitingAt("executePayout");
        execute(job());

        assertThat(pi).isEnded().hasPassed("endPaid").hasNotPassed("releaseHold");
        verify(holdService).captureHold(eq("it-review-ok"), any());
        verify(holdService, never()).releaseHold(any(), any());
    }

    @Test
    void manualReview_rejected_compensatesHold() {
        ProcessInstance pi = start("it-review-no", "ACC-1001", 15000, "REGULAR");

        complete(task(pi), withVariables("approved", false));

        assertThat(pi).isEnded().hasPassed("releaseHold", "endRejected").hasNotPassed("executePayout");
        verify(holdService).releaseHold(eq("it-review-no"), eq("HOLD-it-review-no"));
    }

    @Test
    void slaTimer_escalatesWithoutCancellingReview() {
        ProcessInstance pi = start("it-sla", "ACC-1001", 15000, "REGULAR");

        assertThat(pi).isWaitingAt("reviewWithdrawal");
        execute(job(pi));                                  // fire the SLA timer

        assertThat(pi).hasPassed("escalateReview").isWaitingAt("reviewWithdrawal");
    }

    @Test
    void payoutFailure_retriesThenIncident_thenRecovers() {
        ProcessInstance pi = start("it-fail", "FAIL-3001", 500, "REGULAR");

        for (int attempt = 1; attempt <= 3; attempt++) {
            Assertions.assertThatThrownBy(() -> execute(job(pi)))
                    .hasStackTraceContaining("Payment gateway unavailable");
        }
        Assertions.assertThat(job(pi).getRetries()).isZero();
        Assertions.assertThat(runtimeService.createIncidentQuery()
                .processInstanceId(pi.getId()).count()).isEqualTo(1);

        payoutService.setGatewayUp(true);              // downstream recovers
        managementService.setJobRetries(job(pi).getId(), 1);
        execute(job(pi));

        assertThat(pi).isEnded().hasPassed("endPaid");
    }

    private Object historyVar(ProcessInstance pi, String name) {
        var v = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId()).variableName(name).singleResult();
        return v == null ? null : v.getValue();
    }
}