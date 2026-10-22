package com.divita.withdrawal_workflow.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PayoutService {

    private static final Logger log = LoggerFactory.getLogger(PayoutService.class);
    private final AtomicBoolean gatewayUp = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, String> payouts = new ConcurrentHashMap<>();
    private final AtomicBoolean crashAfterPay = new AtomicBoolean(false);

    public void setGatewayUp(boolean up) { gatewayUp.set(up); }
    public void setCrashAfterPay(boolean v) { crashAfterPay.set(v); }
    public boolean shouldCrashAfterPay() { return crashAfterPay.getAndSet(false); } // one-shot

    public String pay(String requestId, String accountId, BigDecimal amount) {
        String existing = payouts.get(requestId);
        if (existing != null) {
            log.info("Payout already done [{}]: returning txn={}", requestId, existing);
            return existing;
        }
        if (!gatewayUp.get() && accountId != null && accountId.startsWith("FAIL-")) {
            log.error("Payout failed [{}]: gateway unavailable", requestId);
            throw new RuntimeException("Payment gateway unavailable");
        }
        String txnId = "TXN-" + UUID.randomUUID();
        payouts.put(requestId, txnId);
        log.info("Payout sent [{}]: account={} amount={} txn={}", requestId, accountId, amount, txnId);
        return txnId;
    }
}