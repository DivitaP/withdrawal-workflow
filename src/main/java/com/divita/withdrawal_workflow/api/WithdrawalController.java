package com.divita.withdrawal_workflow.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {
    private final WithdrawalService service;

    public WithdrawalController(WithdrawalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StartResponse> submit(@RequestBody WithdrawalRequest request) {
        StartResponse r = service.start(request);
        return ResponseEntity.status(r.created() ? HttpStatus.CREATED : HttpStatus.OK).body(r);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<WithdrawalStatus> status(@PathVariable String requestId) {
        return service.status(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
