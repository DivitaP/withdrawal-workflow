package com.divita.withdrawal_workflow.api;

import com.divita.withdrawal_workflow.domain.PayoutService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PayoutService payoutService;

    public AdminController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @PostMapping("/gateway")
    public String setGateway(@RequestParam boolean up) {
        payoutService.setGatewayUp(up);
        return "gatewayUp=" + up;
    }

    @PostMapping("/crash-after-pay")
    public String crashAfterPay(@RequestParam boolean on) {
        payoutService.setCrashAfterPay(on);
        return "crashAfterPay=" + on;
    }
}