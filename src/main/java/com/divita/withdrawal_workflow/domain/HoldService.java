package com.divita.withdrawal_workflow.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HoldService {

    private static final Logger log = LoggerFactory.getLogger(HoldService.class);
    private final Map<String, BigDecimal> activeHolds = new ConcurrentHashMap<>();

    public String placeHold(String requestId, String accountId, BigDecimal amount) {
        String holdId = "HOLD-" + requestId;          // deterministic → idempotent
        activeHolds.putIfAbsent(holdId, amount);
        log.info("Hold placed [{}]: account={} amount={} hold={}", requestId, accountId, amount, holdId);
        return holdId;
    }

    public void releaseHold(String requestId, String holdId) {
        BigDecimal released = activeHolds.remove(holdId);
        log.info("Hold released [{}]: hold={} amount={}", requestId, holdId,
                released == null ? "already released" : released);
    }

    public void captureHold(String requestId, String holdId) {
        BigDecimal captured = activeHolds.remove(holdId);
        log.info("Hold captured [{}]: hold={} amount={}", requestId, holdId,
                captured == null ? "none" : captured);
    }
}