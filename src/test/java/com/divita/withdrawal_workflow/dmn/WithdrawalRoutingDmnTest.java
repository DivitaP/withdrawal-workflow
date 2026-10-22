package com.divita.withdrawal_workflow.dmn;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class WithdrawalRoutingDmnTest {

    private static DmnEngine dmnEngine;
    private static DmnDecision decision;

    @BeforeAll
    static void setUp() throws Exception {
        dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
        try (InputStream is = WithdrawalRoutingDmnTest.class.getResourceAsStream("/withdrawal-routing.dmn")) {
            decision = dmnEngine.parseDecision("withdrawal-routing", is);
        }
    }

    @ParameterizedTest(name = "funds={0} fraud={1} type={2} amount={3} -> {4}")
    @CsvSource({
            // rule 1: insufficient funds wins over everything
            "false, 10, REGULAR,  500,     REJECT,        Insufficient funds",
            "false, 95, REGULAR,  500,     REJECT,        Insufficient funds",
            // rule 2: fraud >= 80
            "true,  80, REGULAR,  500,     REJECT,        High fraud risk",
            // rule 3: fraud 50..79
            "true,  79, REGULAR,  500,     MANUAL_REVIEW, Elevated fraud score",
            "true,  50, REGULAR,  500,     MANUAL_REVIEW, Elevated fraud score",
            // rule 4: hardship
            "true,  10, HARDSHIP, 500,     MANUAL_REVIEW, Hardship requires review",
            // rule 5: amount boundary
            "true,  10, REGULAR,  10000,   MANUAL_REVIEW, Large amount",
            "true,  10, REGULAR,  9999.99, AUTO_APPROVE,  Within auto limits",
            // rule 6: catch-all
            "true,  49, REGULAR,  500,     AUTO_APPROVE,  Within auto limits"
    })
    void routesCorrectly(boolean sufficientFunds, int fraudScore, String type, double amount,
                         String expectedRoute, String expectedReason) {
        DmnDecisionRuleResult result = dmnEngine.evaluateDecisionTable(decision,
                        Variables.createVariables()
                                .putValue("sufficientFunds", sufficientFunds)
                                .putValue("fraudScore", fraudScore)
                                .putValue("withdrawalType", type)
                                .putValue("amount", amount))
                .getSingleResult();

        assertThat(result).isNotNull();
        assertThat((String) result.getEntry("route")).isEqualTo(expectedRoute);
        assertThat((String) result.getEntry("reason")).isEqualTo(expectedReason);
    }
}